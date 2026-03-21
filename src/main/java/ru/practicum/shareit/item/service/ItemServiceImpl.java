package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemDto create(Long userId, Item item) {
        log.info("Создание вещи '{}' для пользователя с id: {}", item.getName(), userId);

        User owner = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь с id {} не найден", userId);
                    return new NotFoundException("Пользователь не найден");
                });

        item.setOwner(owner);
        Item savedItem = itemRepository.save(item);

        log.debug("{}", savedItem); // Вывод объекта

        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemDto update(Long userId, Long itemId, Item item) {
        log.info("Обновление вещи id: {} пользователем id: {}", itemId, userId);

        Item itemInRepository = findByIdOrException(itemId);

        if (!itemInRepository.getOwner().getId().equals(userId)) {
            log.warn("Доступ запрещен для пользователя id {}", userId);
            throw new ForbiddenException("Редактировать может только владелец");
        }

        if (item.getName() != null) itemInRepository.setName(item.getName());
        if (item.getDescription() != null) itemInRepository.setDescription(item.getDescription());
        if (item.getAvailable() != null) itemInRepository.setAvailable(item.getAvailable());

        Item updatedItem = itemRepository.save(itemInRepository);
        log.debug("{}", updatedItem); // Вывод объекта

        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemDto getById(Long itemId) {
        log.info("Получение вещи по id: {}", itemId);
        Item item = findByIdOrException(itemId);
        log.debug("{}", item); // Вывод объекта
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getAllByOwner(Long userId) {
        log.info("Получение всех вещей владельца id: {}", userId);

        List<Item> items = itemRepository.findAll().stream()
                .filter(item -> item.getOwner().getId().equals(userId))
                .toList();

        log.debug("{}", items); // Вывод списка объектов
        return items.stream().map(ItemMapper::toItemDto).toList();
    }

    @Override
    public List<ItemDto> search(String text) {
        log.info("Поиск вещей по запросу: '{}'", text);

        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        List<Item> results = itemRepository.findAll().stream()
                .filter(Item::getAvailable)
                .filter(item -> item.getName().toLowerCase().contains(text.toLowerCase())
                        || item.getDescription().toLowerCase().contains(text.toLowerCase()))
                .toList();

        log.debug("{}", results); // Вывод найденных объектов
        return results.stream().map(ItemMapper::toItemDto).toList();
    }

    private Item findByIdOrException(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.warn("Вещь с id {} не найдена ", itemId);
                    return new NotFoundException("Вещь не найдена");
                });
    }
}
