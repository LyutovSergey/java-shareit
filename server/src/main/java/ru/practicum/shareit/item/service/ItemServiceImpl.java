package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dao.CommentRepository;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dao.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    @Transactional
    public ItemDto create(Long userId, ItemDto itemDto) {
        log.info("Создание вещи '{}' для пользователя с id: {}", itemDto.getName(), userId);
        User owner = userService.findByIdOrException(userId);
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);
        if (itemDto.getRequestId() != null) {
            ItemRequest request = itemRequestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Запрос  не найден"));
            item.setRequest(request);
        }
        Item savedItem = itemRepository.save(item);
        log.debug("{}", savedItem); // Вывод объекта
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    @Transactional
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        log.info("Обновление вещи id: {} пользователем id: {}", itemId, userId);
        userService.findByIdOrException(userId);
        Item itemInRepository = findItemByIdOrException(itemId);

        if (!itemInRepository.getOwner().getId().equals(userId)) {
            log.warn("Доступ запрещен для пользователя id {}", userId);
            throw new ForbiddenException("Редактировать может только владелец");
        }

        if (itemDto.getName() != null && !itemDto.getName().isBlank())
            itemInRepository.setName(itemDto.getName());
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank())
            itemInRepository.setDescription(itemDto.getDescription());
        if (itemDto.getAvailable() != null) itemInRepository.setAvailable(itemDto.getAvailable());

        Item updatedItem = itemRepository.save(itemInRepository);
        log.debug("{}", updatedItem); // Вывод объекта

        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemDto getById(Long itemId, Long userId) {
        log.info("Получение вещи id: {} пользователем id: {}", itemId, userId);
        Item item = findItemByIdOrException(itemId);
        ItemDto itemDto = ItemMapper.toItemDto(item);

        //Добавляем комментарии
        itemDto.setComments(commentRepository.findAllByItemId(itemId).stream()
                .map(CommentMapper::toDto).toList());

        // Бронирования добавляем только если запрашивает владелец
         if (item.getOwner().getId().equals(userId)) {
            setBookings(itemDto);
        }
        return itemDto;
    }

    @Override
    public List<ItemDto> getAllByOwner(Long userId) {
        log.info("Получение всех вещей владельца id: {}", userId);

        userService.findByIdOrException(userId);

        List<Item> items = itemRepository.findAllByOwnerId(userId);
        List<Long> itemIds = items.stream().map(Item::getId).toList();
        List<Booking> allBookings = bookingRepository.findAllByItemIds(itemIds);
        List<Comment> allComments = commentRepository.findAllByItemIdIn(itemIds);
        Map<Long, List<Comment>> commentsMap = allComments.stream()
                .collect(Collectors.groupingBy(c -> c.getItem().getId()));
        Map<Long, List<Booking>> bookingsMap = allBookings.stream()
                .collect(Collectors.groupingBy(booking -> booking.getItem().getId()));

        List<ItemDto> itemDtos = items.stream()
                .map(item -> {
                    ItemDto itemDto = ItemMapper.toItemDto(item);
                    List<Booking> itemBookings = bookingsMap.getOrDefault(item.getId(), List.of());
                    setBookingsForList(itemDto, itemBookings);

                    List<Comment> itemComments = commentsMap.getOrDefault(item.getId(), List.of());
                    itemDto.setComments(itemComments.stream()
                            .map(CommentMapper::toDto)
                            .toList());

                    return itemDto;
                })
                .toList(); // Собираем все обработанные DTO в итоговый список



        log.debug("{}", itemDtos); // Вывод списка объектов
        return itemDtos;
    }

    @Override
    public List<ItemDto> search(String text) {
        log.info("Поиск вещей по запросу: '{}'", text);

        List<Item> results = itemRepository.search(text);

        log.debug("{}", results); // Вывод найденных объектов
        return results.stream().map(ItemMapper::toItemDto).toList();
    }

    @Override
    @Transactional
    public CommentDto createComment(Long userId, Long itemId, CommentDto commentDto) {
        log.info("Добавление комментария пользователем {} для вещи {}", userId, itemId);

        LocalDateTime now = LocalDateTime.now();

        // 1. Проверяем, что пользователь реально брал вещь и срок аренды уже ИСТЕК
        // Используем метод из BookingRepository
        boolean hasFinishedBooking = bookingRepository
                .findFirstByItemIdAndBookerIdAndStatusAndEndBefore(
                        itemId, userId, BookingStatus.APPROVED, now)
                .isPresent();

        if (!hasFinishedBooking) {
            log.warn("Пользователь {} не может оставить отзыв на вещь {}", userId, itemId);
            throw new BadRequestException("Вы не можете оставить отзыв на вещь, " +
                    "которую не бронировали или срок аренды еще не истек.");
        }

        // 2. Находим автора и вещь
        User author = userService.findByIdOrException(userId);
        Item item = findItemByIdOrException(itemId);

        // 3. Маппим DTO в сущность, сохраняем и возвращаем DTO обратно
        Comment comment = CommentMapper.toComment(commentDto, item, author);
        Comment savedComment = commentRepository.save(comment);

        log.debug("Комментарий сохранен: {}", savedComment);

        return CommentMapper.toDto(savedComment);
    }


    @Override
    public Item findItemByIdOrException(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.warn("Вещь с id {} не найдена ", itemId);
                    return new NotFoundException("Вещь с id " + itemId + " не найдена");
                });
    }

    private void setBookings(ItemDto dto) {
        LocalDateTime now = LocalDateTime.now();

        // Ищем последнее (last) бронирование: завершенное или текущее
        dto.setLastBooking(bookingRepository
                .findFirstByItemIdAndStatusAndStartBeforeOrderByStartDesc(
                        dto.getId(), BookingStatus.APPROVED, now)
                .map(b -> new ItemDto.BookingShortDto(b.getId(), b.getBooker().getId()))
                .orElse(null));

        // Ищем следующее (next) бронирование: запланированное в будущем
        dto.setNextBooking(bookingRepository
                .findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(
                        dto.getId(), BookingStatus.APPROVED, now)
                .map(b -> new ItemDto.BookingShortDto(b.getId(), b.getBooker().getId()))
                .orElse(null));
    }

    private void setBookingsForList(ItemDto dto, List<Booking> bookings) {
        LocalDateTime now = LocalDateTime.now();
        Booking last = bookings.stream()
                .filter(b -> !b.getStart().isAfter(now))
                .reduce((first, second) -> second) // Список отсортирован по ASC, берем последний подходящий
                .orElse(null);

        Booking next = bookings.stream()
                .filter(b -> b.getStart().isAfter(now))
                .findFirst()
                .orElse(null);

        if (last != null) dto.setLastBooking(new ItemDto.BookingShortDto(last.getId(), last.getBooker().getId()));
        if (next != null) dto.setNextBooking(new ItemDto.BookingShortDto(next.getId(), next.getBooker().getId()));
    }
}
