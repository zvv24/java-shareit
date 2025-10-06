package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.client.BookingClient;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.util.Constants;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {
    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> createBooking(@RequestBody BookingDto bookingDto,
                                                @RequestHeader(Constants.HEAD) Integer userId) {
        return bookingClient.createBooking(bookingDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> updateBookingStatus(@PathVariable Integer bookingId,
                                                      @RequestParam(required = true) Boolean approved,
                                                      @RequestHeader(Constants.HEAD) Long userId) {
        return bookingClient.updateBookingStatus(bookingId, approved, userId);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBookingById(@PathVariable Integer bookingId,
                                                 @RequestHeader(Constants.HEAD) Long userId) {
        return bookingClient.getBookingById(bookingId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getUserBookings(@RequestParam(defaultValue = "ALL") BookingState state,
                                                  @RequestHeader(Constants.HEAD) Integer userId) {
        return bookingClient.getUserBookings(userId, state);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getOwnerBookings(@RequestParam(defaultValue = "ALL") BookingState state,
                                                   @RequestHeader(Constants.HEAD) Integer userId) {
        return bookingClient.getOwnerBookings(userId, state);
    }
}
