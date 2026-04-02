package ru.practicum.shareit.booking.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findFirstByItemIdAndBookerIdAndStatusAndEndBefore(
            Long itemId, Long userId, BookingStatus status, LocalDateTime now);

    Optional<Booking> findFirstByItemIdAndStatusAndStartBeforeOrderByStartDesc(
            Long itemId, BookingStatus status, LocalDateTime now);

    Optional<Booking> findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(
            Long itemId, BookingStatus status, LocalDateTime now);

    @Query("select b from Booking b " +
            "where b.booker.id = ?1 " +
            "and (?2 = 'ALL' " +
            "  or (?2 = 'CURRENT' and b.start <= ?3 and b.end >= ?3) " +
            "  or (?2 = 'PAST' and b.end < ?3) " +
            "  or (?2 = 'FUTURE' and b.start > ?3) " +
            "  or (?2 = 'WAITING' and b.status = 'WAITING') " +
            "  or (?2 = 'REJECTED' and b.status = 'REJECTED')) " +
            "order by b.start desc")
    List<Booking> findAllByBookerFiltered(Long bookerId, String state, LocalDateTime now);

    @Query("select b from Booking b " +
            "where b.item.owner.id = ?1 " +
            "and (?2 = 'ALL' " +
            "  or (?2 = 'CURRENT' and b.start <= ?3 and b.end >= ?3) " +
            "  or (?2 = 'PAST' and b.end < ?3) " +
            "  or (?2 = 'FUTURE' and b.start > ?3) " +
            "  or (?2 = 'WAITING' and b.status = 'WAITING') " +
            "  or (?2 = 'REJECTED' and b.status = 'REJECTED')) " +
            "order by b.start desc")
    List<Booking> findAllByOwnerFiltered(Long ownerId, String state, LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.item i " +
            "WHERE i.id IN ?1"+
            "AND b.status = 'APPROVED' " +
            "order by b.start desc")
    List<Booking> findAllByItemIds(List<Long> itemIds);
}
