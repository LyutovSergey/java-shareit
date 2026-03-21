package ru.practicum.shareit.item.dao;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;

@Repository
public class ItemRepository {
    private final Map<Long, Item> items = new HashMap<>();
    private Long idCounter = 1L;

    public Item save(Item item) {
        if (item.getId() == null) item.setId(idCounter++);
        items.put(item.getId(), item);
        return item;
    }

    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(items.get(id));
    }

    public List<Item> findAllByOwner(Long ownerId) {
        return items.values().stream()
                .filter(item -> item.getOwner().getId().equals(ownerId))
                .toList();
    }

    public List<Item> search(String text) {
        if (text.isBlank()) return Collections.emptyList();
        String query = text.toLowerCase();
        return items.values().stream()
                .filter(Item::getAvailable)
                .filter(item -> item.getName().toLowerCase().contains(query)
                        || item.getDescription().toLowerCase().contains(query))
                .toList();
    }

    public List<Item> findAll() {
        return new ArrayList<>(items.values());
    }
}
