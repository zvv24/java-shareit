package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingFullDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.util.List;

public interface BookingService {
    BookingFullDto createBooking(BookingDto bookingDto, Integer userId);

    BookingFullDto updateBookingStatus(Integer bookingId, BookingStatus status, Integer userId);

    BookingFullDto getBookingById(Integer bookingId, Integer userId);

    List<BookingFullDto> getUserBookings(Integer userId, BookingState state);

    List<BookingFullDto> getOwnerBookings(Integer ownerId, BookingState state);
}
