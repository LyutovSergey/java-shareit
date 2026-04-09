package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.BookingStateForFilter;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemService itemService;
    private final UserService userService;

    @Override
    @Transactional
    public BookingResponseDto create(Long userId, BookingDto bookingDto) {
        log.info("Создание бронирования пользователь {}, бронь {}", userId, bookingDto);
        User user = userService.findByIdOrException(userId);
        Item item = itemService.findItemByIdOrException(bookingDto.getItemId());

        if (!item.getAvailable()) throw new BadRequestException("Вещь недоступна");
        if (item.getOwner().getId().equals(userId)) throw new BadRequestException("Нельзя бронировать свою вещь");
        if (bookingDto.getEnd().isBefore(bookingDto.getStart()) || bookingDto.getEnd().isEqual(bookingDto.getStart())) {
            throw new BadRequestException("Неверные даты бронирования");
        }

        Booking booking = BookingMapper.toNewBooking(bookingDto, item, user);

        return BookingMapper.toResponseDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingResponseDto approve(Long userId, Long bookingId, Boolean approved) {
        log.info("Запрос подтверждения бронирования. Пользователь {}, бронь {}, статус {}", userId, bookingId, approved);
        Booking booking = findByIdOrException(bookingId);
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Только владелец может подтвердить бронирование");
        }
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new BadRequestException("Статус уже изменен");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return BookingMapper.toResponseDto(bookingRepository.save(booking));
    }

    @Override
    public BookingResponseDto getById(Long userId, Long bookingId) {
        log.info("Запрос информации о  бронировании. Пользователь {}, бронь {}", userId, bookingId);
        Booking booking = findByIdOrException(bookingId);
        if (!booking.getBooker().getId().equals(userId) &&
                !booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("Нет прав на просмотр");
        }
        return BookingMapper.toResponseDto(booking);
    }

    @Override
    public List<BookingResponseDto> getAllByBooker(Long userId, String stateStr) {
        log.info("Запрос списка бронирований пользователя {}, статус {}", userId, stateStr);
        userService.getById(userId); // Проверка на существование

        BookingStateForFilter state = getBookingStateForFilterOrException(stateStr);

        return bookingRepository.findAllByBookerFiltered(userId, state.name(), LocalDateTime.now())
                .stream().map(BookingMapper::toResponseDto).toList();
    }

    @Override
    public List<BookingResponseDto> getAllByOwner(Long userId, String stateStr) {
        log.info("Получение списка бронирований вещей владельца {}, статус {}", userId, stateStr);
        userService.getById(userId); // Проверка на существование

        BookingStateForFilter state = getBookingStateForFilterOrException(stateStr);

        return bookingRepository.findAllByOwnerFiltered(userId, state.name(), LocalDateTime.now())
                .stream().map(BookingMapper::toResponseDto).toList();
    }

    private BookingStateForFilter getBookingStateForFilterOrException(String stateStr) {
        return BookingStateForFilter.from(stateStr).orElseThrow(() -> {
            log.warn("Фильтр статуса некорректный: {} ", stateStr);
            return new BadRequestException("Статус  " + stateStr + " некорректный");
        });
    }

    private Booking findByIdOrException(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.warn("Бронирование с id {} не найдено", bookingId);
                    return new NotFoundException("Бронирование с id " + bookingId + " не найдено");
                });

    }

}
