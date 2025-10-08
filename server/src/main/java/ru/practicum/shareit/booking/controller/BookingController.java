package ru.practicum.shareit.booking.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingFullDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.util.Constants;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@AllArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingFullDto createBooking(@RequestBody BookingDto bookingDto,
                                        @RequestHeader(Constants.HEAD) Integer userId) {
        return bookingService.createBooking(bookingDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingFullDto updateBookingStatus(@PathVariable Integer bookingId,
                                              @RequestParam(required = true) Boolean approved,
                                              @RequestHeader(Constants.HEAD) Integer userId) {
        BookingStatus status = approved ? BookingStatus.APPROVED : BookingStatus.REJECTED;
        return bookingService.updateBookingStatus(bookingId, status, userId);
    }

    @GetMapping("/{bookingId}")
    public BookingFullDto getBookingById(@PathVariable Integer bookingId,
                                         @RequestHeader(Constants.HEAD) Integer userId) {
        return bookingService.getBookingById(bookingId, userId);
    }

    @GetMapping
    public List<BookingFullDto> getUserBookings(@RequestParam(defaultValue = "ALL") String state,
                                                @RequestHeader(Constants.HEAD) Integer userId) {
        return bookingService.getUserBookings(userId, BookingState.valueOf(state));
    }

    @GetMapping("/owner")
    public List<BookingFullDto> getOwnerBookings(@RequestParam(defaultValue = "ALL") String state,
                                                 @RequestHeader(Constants.HEAD) Integer userId) {
        return bookingService.getOwnerBookings(userId, BookingState.valueOf(state));
    }
}
