package ru.practicum.shareit.booking.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByBookerIdOrderByStartDesc(Long bookerId);

    List<Booking> findAllByItemOwnerIdOrderByStartDesc(Long ownerId);

    List<Booking> findAllByBookerIdAndStatusOrderByStartDesc(Long bookerId, BookingStatus status);

    Optional<Booking> findFirstByItemIdAndBookerIdAndStatusAndEndBefore(
            Long itemId, Long userId, BookingStatus status, LocalDateTime now);

    Optional<Booking> findFirstByItemIdAndStatusAndStartBeforeOrderByStartDesc(
            Long itemId, BookingStatus status, LocalDateTime now);

    Optional<Booking> findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(
            Long itemId, BookingStatus status, LocalDateTime now);
}
