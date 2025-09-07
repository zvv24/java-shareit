package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingFullDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

public class BookingMapper {
    public static BookingFullDto mapToBookingFullDto(Booking booking) {
        BookingFullDto dto = new BookingFullDto();
        dto.setId(booking.getId());
        dto.setStart(booking.getStart());
        dto.setEnd(booking.getEnd());

        ItemDto itemDto = new ItemDto();
        itemDto.setId(booking.getItem().getId());
        itemDto.setName(booking.getItem().getName());
        itemDto.setDescription(booking.getItem().getDescription());
        itemDto.setAvailable(booking.getItem().getAvailable());
        dto.setItem(itemDto);

        UserDto userDto = new UserDto();
        userDto.setId(booking.getBooker().getId());
        userDto.setName(booking.getBooker().getName());
        userDto.setEmail(booking.getBooker().getEmail());
        dto.setBooker(userDto);

        dto.setStatus(booking.getStatus());
        return dto;
    }
}
