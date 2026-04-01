package ru.practicum.shareit.item.dao;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    Item save(Item item);

    Optional<Item> findById(Long id);

    List<Item> findAllByOwner(Long ownerId);

    List<Item> search(String text);

    List<Item> findAll();
}
