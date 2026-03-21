package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ItemDto create(@RequestHeader("X-Sharer-User-Id") Long userId,
                          @Valid @RequestBody Item item) {
        return itemService.create(userId, item);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader("X-Sharer-User-ID") Long userId,
                          @PathVariable Long itemId,
                          @RequestBody ItemDto itemDto) { // Используем DTO!
        return itemService.update(userId, itemId, ItemMapper.toItem(itemDto));
    }

    @GetMapping("/{itemId}")
    public ItemDto getById(@PathVariable Long itemId) {
        return itemService.getById(itemId);
    }

    @GetMapping
    public List<ItemDto> getAll(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.getAllByOwner(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text) {
        return itemService.search(text);
    }
}