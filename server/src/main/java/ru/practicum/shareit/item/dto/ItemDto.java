package ru.practicum.shareit.item.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder(toBuilder = true)
public class ItemDto {
    private Long id;


    private String name;
    private String description;


    private Boolean available; // статус доступности
    private Long ownerId;        // владелец
    private Long requestId; // ссылка на запрос, если создано по запросу
    // Информация о бронированиях
    private BookingShortDto lastBooking;
    private BookingShortDto nextBooking;

    // Список комментариев
    private List<CommentDto> comments;

    @Data
    @AllArgsConstructor
    public static class BookingShortDto {
        private Long id;
        private Long bookerId;
    }
}
