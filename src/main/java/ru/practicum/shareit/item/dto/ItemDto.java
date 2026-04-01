package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

/**
 * TODO Sprint add-controllers.
 */

@Data
@Builder(toBuilder = true)
public class ItemDto {
    private Long id;

    @NotBlank(message = "Имя вещи не может быть пустым")
    private String name;
    @NotBlank(message = "Описание вещи не может быть пустым")
    private String description;

    @NotNull
    private Boolean available; // статус доступности
    private Long ownerId;        // владелец
    private Long requestId; // ссылка на запрос, если создано по запросу
}
