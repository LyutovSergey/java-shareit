package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * TODO Sprint add-item-requests.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemRequestDto {
    private Long id;

    @NotBlank(message = "Описание запроса не может быть пустым")
    private String description;

    private LocalDateTime created; // Дата создания

    // Список вещей, предложенных в ответ (заполняем при GET запросах)
    private List<ItemAnswerDto> items;

    /**
     * Внутренний класс для краткого описания вещи в ответе на запрос
     * (как требует ТЗ: id вещи, название, id владельца)
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ItemAnswerDto {
        private Long id;
        private String name;
        private String description;
        private Boolean available;
        private Long requestId;
        private Long ownerId;
    }
}