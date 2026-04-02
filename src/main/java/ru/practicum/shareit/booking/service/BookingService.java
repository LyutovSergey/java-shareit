package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;

import java.util.List;

public interface BookingService {
    BookingResponseDto create(Long userId, BookingDto bookingDto);
    BookingResponseDto approve(Long userId, Long bookingId, Boolean approved);
    BookingResponseDto getById(Long userId, Long bookingId);
    List<BookingResponseDto> getAllByBooker(Long userId, String state);
    List<BookingResponseDto> getAllByOwner(Long userId, String state);
}

