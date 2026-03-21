package ru.practicum.shareit.item.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.model.User;

/**
 * TODO Sprint add-controllers.
 */
@Data
@Builder(toBuilder = true)
public class Item {
    private Long id;

    @NotBlank(message = "Имя вещи не может быть пустым")
    private String name;
    @NotBlank(message = "Описание вещи не может быть пустым")
    private String description;

    @NotNull
    private Boolean available; // статус доступности
    private User owner;        // владелец
    private ItemRequest request; // ссылка на запрос, если создано по запросу
}