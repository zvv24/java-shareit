package ru.practicum.shareit.booking;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingFullDto;

import java.util.List;

/**
 * TODO Sprint add-bookings.
 */
@RestController
@RequestMapping(path = "/bookings")
@AllArgsConstructor
public class BookingController {
    private final BookingService bookingService;
    private static final String HEAD = "X-Sharer-User-Id";

    @PostMapping
    public BookingFullDto createBooking(@RequestBody BookingDto bookingDto,
                                        @RequestHeader(HEAD) Integer userId) {
        return bookingService.createBooking(bookingDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingFullDto updateBookingStatus(@PathVariable Integer bookingId,
                                              @RequestParam Boolean approved,
                                              @RequestHeader(HEAD) Integer userId) {
        BookingStatus status = approved ? BookingStatus.APPROVED : BookingStatus.REJECTED;
        return bookingService.updateBookingStatus(bookingId, status, userId);
    }

    @GetMapping("/{bookingId}")
    public BookingFullDto getBookingById(@PathVariable Integer bookingId,
                                         @RequestHeader(HEAD) Integer userId) {
        return bookingService.getBookingById(bookingId, userId);
    }

    @GetMapping
    public List<BookingFullDto> getUserBookings(@RequestParam(defaultValue = "ALL") String state,
                                                @RequestHeader(HEAD) Integer userId) {
        return bookingService.getUserBookings(userId, BookingState.valueOf(state));
    }

    @GetMapping("/owner")
    public List<BookingFullDto> getOwnerBookings(@RequestParam(defaultValue = "ALL") String state,
                                                 @RequestHeader(HEAD) Integer userId) {
        return bookingService.getOwnerBookings(userId, BookingState.valueOf(state));
    }
}
