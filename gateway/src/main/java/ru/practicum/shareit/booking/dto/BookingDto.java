package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;

public class BookingDto {
    @NotNull
    private Integer id;
    @NotNull
    @FutureOrPresent
    private LocalDateTime start;
    @NotNull
    @Future
    private LocalDateTime end;
    @NotBlank
    private Integer itemId;
    @NotBlank
    private Integer booker;
    @NotNull
    private BookingStatus status;
}
