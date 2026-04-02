package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public BookingResponseDto create(Long userId, BookingDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (!item.getAvailable()) throw new BadRequestException("Вещь недоступна");
        if (item.getOwner().getId().equals(userId)) throw new NotFoundException("Нельзя бронировать свою вещь");
        if (dto.getEnd().isBefore(dto.getStart()) || dto.getEnd().isEqual(dto.getStart())) {
            throw new BadRequestException("Неверные даты бронирования");
        }

        Booking booking = Booking.builder()
                .start(dto.getStart())
                .end(dto.getEnd())
                .item(item)
                .booker(user)
                .status(BookingStatus.WAITING)
                .build();
        return BookingMapper.toResponseDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingResponseDto approve(Long userId, Long bookingId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Только владелец может подтвердить бронирование");
        }
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new BadRequestException("Статус уже изменен");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return BookingMapper.toResponseDto(bookingRepository.save(booking));
    }

    @Override
    public BookingResponseDto getById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));
        if (!booking.getBooker().getId().equals(userId) &&
                !booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("Нет прав на просмотр");
        }
        return BookingMapper.toResponseDto(booking);
    }

    @Override
    public List<BookingResponseDto> getAllByBooker(Long userId, String stateStr) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        LocalDateTime now = LocalDateTime.now();
        return bookingRepository.findAllByBookerIdOrderByStartDesc(userId).stream()
                .map(BookingMapper::toResponseDto)
                .toList();
    }

    @Override
    public List<BookingResponseDto> getAllByOwner(Long userId, String stateStr) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        return bookingRepository.findAllByItemOwnerIdOrderByStartDesc(userId).stream()
                .map(BookingMapper::toResponseDto)
                .toList();
    }
}
