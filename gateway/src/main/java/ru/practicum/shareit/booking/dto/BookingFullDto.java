package ru.practicum.shareit.booking.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingFullDto {
    @NotNull
    private Integer id;
    @NotNull
    private LocalDateTime start;
    @NotNull
    private LocalDateTime end;
    @Valid
    @NotNull
    private ItemDto item;
    @Valid
    @NotNull
    private UserDto booker;
    @NotNull
    private BookingStatus status;
}
