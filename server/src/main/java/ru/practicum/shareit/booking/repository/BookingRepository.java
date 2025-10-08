package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    List<Booking> findByBookerIdOrderByStartDesc(Integer bookerId);

    List<Booking> findByBookerIdAndEndBeforeOrderByStartDesc(Integer bookerId, LocalDateTime end);

    List<Booking> findByBookerIdAndStartAfterOrderByStartDesc(Integer bookerId, LocalDateTime start);

    List<Booking> findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
            Integer bookerId, LocalDateTime start, LocalDateTime end);

    List<Booking> findByBookerIdAndStatusOrderByStartDesc(Integer bookerId, BookingStatus status);

    List<Booking> findByItemOwnerIdOrderByStartDesc(Integer ownerId);

    List<Booking> findByItemOwnerIdAndEndBeforeOrderByStartDesc(Integer ownerId, LocalDateTime end);

    List<Booking> findByItemOwnerIdAndStartAfterOrderByStartDesc(Integer ownerId, LocalDateTime start);

    List<Booking> findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
            Integer ownerId, LocalDateTime start, LocalDateTime end);

    List<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(Integer ownerId, BookingStatus status);

    Optional<Booking> findFirstByItemIdAndEndBeforeOrderByEndDesc(Integer itemId, LocalDateTime date);

    Optional<Booking> findFirstByItemIdAndStartAfterOrderByStartAsc(Integer itemId, LocalDateTime date);

    boolean existsByItemIdAndBookerIdAndEndBefore(Integer itemId, Integer bookerId, LocalDateTime date);

    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.status IN ('APPROVED', 'WAITING') " +
            "AND (:start < b.end AND :end > b.start)")
    boolean existsOverlappingBookings(@Param("itemId") Integer itemId,
                                      @Param("start") LocalDateTime start,
                                      @Param("end") LocalDateTime end);
}
