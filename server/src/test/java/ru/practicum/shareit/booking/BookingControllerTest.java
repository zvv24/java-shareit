package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingFullDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.util.Constants;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @Test
    void createBookingMustReturnBooking() throws Exception {
        Integer userId = 1;

        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(1);
        bookingDto.setStart(LocalDateTime.now().plusHours(1));
        bookingDto.setEnd(LocalDateTime.now().plusHours(2));

        BookingFullDto bookingFullDto = new BookingFullDto();
        bookingFullDto.setId(1);
        bookingFullDto.setStart(LocalDateTime.now().plusHours(1));
        bookingFullDto.setEnd(LocalDateTime.now().plusHours(2));
        bookingFullDto.setStatus(BookingStatus.WAITING);

        when(bookingService.createBooking(any(BookingDto.class), eq(userId)))
                .thenReturn(bookingFullDto);

        mockMvc.perform(post("/bookings")
                        .header(Constants.HEAD, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingFullDto.getId()))
                .andExpect(jsonPath("$.status").value(bookingFullDto.getStatus().toString()));
    }

    @Test
    void updateBookingStatusMustReturnUpdatedBooking() throws Exception {
        Integer userId = 1;
        Integer bookingId = 1;

        BookingFullDto bookingFullDto = new BookingFullDto();
        bookingFullDto.setId(1);
        bookingFullDto.setStatus(BookingStatus.APPROVED);

        when(bookingService.updateBookingStatus(eq(bookingId), eq(BookingStatus.APPROVED), eq(userId)))
                .thenReturn(bookingFullDto);

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(Constants.HEAD, userId)
                        .param("approved", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void getBookingByIdMustReturnBooking() throws Exception {
        Integer userId = 1;
        Integer bookingId = 2;

        BookingFullDto bookingFullDto = new BookingFullDto();
        bookingFullDto.setId(1);

        when(bookingService.getBookingById(eq(bookingId), eq(userId)))
                .thenReturn(bookingFullDto);

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header(Constants.HEAD, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingFullDto.getId()));
    }

    @Test
    void getUserBookingsMustReturnBookings() throws Exception {
        Integer userId = 1;
        String state = "ALL";

        BookingFullDto bookingFullDto = new BookingFullDto();
        bookingFullDto.setId(1);
        List<BookingFullDto> bookings = List.of(bookingFullDto);

        when(bookingService.getUserBookings(eq(userId), eq(BookingState.ALL)))
                .thenReturn(bookings);

        mockMvc.perform(get("/bookings")
                        .header(Constants.HEAD, userId)
                        .param("state", state))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(bookings.get(0).getId()));
    }

    @Test
    void getOwnerBookingsMustReturnBookings() throws Exception {
        Integer userId = 1;
        String state = "ALL";

        BookingFullDto bookingFullDto = new BookingFullDto();
        bookingFullDto.setId(1);
        List<BookingFullDto> bookings = List.of(bookingFullDto);

        when(bookingService.getOwnerBookings(eq(userId), eq(BookingState.ALL)))
                .thenReturn(bookings);

        mockMvc.perform(get("/bookings/owner")
                        .header(Constants.HEAD, userId)
                        .param("state", state))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(bookings.get(0).getId()));
    }

    @Test
    void getUserBookingsWithDifferentStatesMustReturnBookings() throws Exception {
        Integer id = 1;
        BookingFullDto bookingFullDto = new BookingFullDto();
        bookingFullDto.setId(1);
        List<BookingFullDto> bookings = List.of(bookingFullDto);

        when(bookingService.getUserBookings(eq(id), any(BookingState.class))).thenReturn(bookings);

        for (BookingState state : BookingState.values()) {
            mockMvc.perform(get("/bookings")
                            .header(Constants.HEAD, id)
                            .param("state", state.toString()))
                    .andExpect(status().isOk());
        }
    }
}