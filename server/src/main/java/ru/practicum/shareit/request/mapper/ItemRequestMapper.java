package ru.practicum.shareit.request.mapper;


import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.ArrayList;
import java.util.stream.Collectors;

@UtilityClass
public class ItemRequestMapper {

    public static ItemRequest toEntity(ItemRequestDto dto) {
        return ItemRequest.builder()
                .description(dto.getDescription())
                .build();
    }

    public static ItemRequestDto toDto(ItemRequest entity) {
        return ItemRequestDto.builder()
                .id(entity.getId())
                .description(entity.getDescription())
                .created(entity.getCreated())
                .items(entity.getItems() != null ?
                        entity.getItems().stream()
                                .map(ItemRequestMapper::toAnswerDto)
                                .collect(Collectors.toList())
                        : new ArrayList<>())
                .build();
    }

    private static ItemRequestDto.ItemAnswerDto toAnswerDto(Item item) {
        return ItemRequestDto.ItemAnswerDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(item.getRequest() != null ? item.getRequest().getId() : null)
                .ownerId(item.getOwner().getId()) // Тот самый Long ownerId
                .build();
    }
}
