package ru.practicum.shareit.booking.dto;


import lombok.Data;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */
@Data
public class BookingDto {

    private Long itemId;


    private LocalDateTime start;


    private LocalDateTime end;
}

