package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingFullDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class BookingServiceTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void createBookingMustCreateReservation() {
        User owner = userRepository.save(new User(null, "Owner", "Owner@email.com"));
        User booker = userRepository.save(new User(null, "Booker", "Booker@email.com"));
        Item item = itemRepository.save(new Item(null, "Item", "Description",
                true, owner, null));

        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusHours(3));
        bookingDto.setEnd(LocalDateTime.now().plusHours(5));

        BookingFullDto result = bookingService.createBooking(bookingDto, booker.getId());

        assertNotNull(result);
        assertEquals(item.getId(), result.getItem().getId());
        assertEquals(booker.getId(), result.getBooker().getId());
    }

    @Test
    void createBookingWithUnavailableItemMustThrowException() {
        User owner = userRepository.save(new User(null, "Owner", "Owner@email.com"));
        User booker = userRepository.save(new User(null, "Booker", "Booker@email.com"));
        Item item = itemRepository.save(new Item(null, "Item", "Description",
                false, owner, null));

        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        assertThrows(ValidationException.class,
                () -> bookingService.createBooking(bookingDto, booker.getId()));
    }

    @Test
    void getUserBookingsMustReturnUserBookings() {
        User owner = userRepository.save(new User(null, "Owner", "Owner@email.com"));
        User booker = userRepository.save(new User(null, "Booker", "Booker@email.com"));
        Item item = itemRepository.save(new Item(null, "Item", "Description",
                true, owner, null));

        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        bookingService.createBooking(bookingDto, booker.getId());

        List<BookingFullDto> result = bookingService.getUserBookings(booker.getId(), BookingState.ALL);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }
}
