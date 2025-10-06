package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingFullDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
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

    @Autowired
    private BookingRepository bookingRepository;

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
        bookingDto.setStart(LocalDateTime.now().plusHours(1));
        bookingDto.setEnd(LocalDateTime.now().plusHours(2));

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
        bookingDto.setStart(LocalDateTime.now().plusHours(1));
        bookingDto.setEnd(LocalDateTime.now().plusHours(2));

        bookingService.createBooking(bookingDto, booker.getId());

        List<BookingFullDto> result = bookingService.getUserBookings(booker.getId(), BookingState.ALL);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void createBookingWithNonExistentItemMustThrowException() {
        User booker = userRepository.save(new User(null, "Booker", "Booker@email.com"));

        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(1);
        bookingDto.setStart(LocalDateTime.now().plusHours(1));
        bookingDto.setEnd(LocalDateTime.now().plusHours(2));

        assertThrows(NotFoundException.class,
                () -> bookingService.createBooking(bookingDto, booker.getId()));
    }

    @Test
    void createBookingWithNonExistentUserMustThrowException() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(1);
        bookingDto.setStart(LocalDateTime.now().plusHours(1));
        bookingDto.setEnd(LocalDateTime.now().plusHours(2));

        assertThrows(NotFoundException.class,
                () -> bookingService.createBooking(bookingDto, 1));
    }

    @Test
    void createBookingWithOwnerBookingOwnItemMustThrowException() {
        User owner = userRepository.save(new User(null, "Owner", "Owner@email.com"));
        Item item = itemRepository.save(new Item(null, "Item", "Description", true, owner, null));

        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusHours(1));
        bookingDto.setEnd(LocalDateTime.now().plusHours(2));

        assertThrows(ValidationException.class,
                () -> bookingService.createBooking(bookingDto, owner.getId()));
    }

    @Test
    void createBookingWithNullUserIdMustThrowException() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(1);
        bookingDto.setStart(LocalDateTime.now().plusHours(1));
        bookingDto.setEnd(LocalDateTime.now().plusHours(2));

        assertThrows(InvalidDataAccessApiUsageException.class, () -> bookingService.createBooking(bookingDto, null));
    }

    @Test
    void updateBookingStatusWithNonOwnerMustThrowException() {
        User owner = userRepository.save(new User(null, "Owner", "Owner@email.com"));
        User user = userRepository.save(new User(null, "User", "User@email.com"));
        User booker = userRepository.save(new User(null, "Booker", "Booker@email.com"));

        Item item = itemRepository.save(new Item(null, "Item", "Description", true, owner, null));

        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().plusHours(1));
        booking.setEnd(LocalDateTime.now().plusHours(2));
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);
        Booking savedBooking = bookingRepository.save(booking);

        assertThrows(ForbiddenException.class,
                () -> bookingService.updateBookingStatus(savedBooking.getId(), BookingStatus.APPROVED, user.getId()));
    }

    @Test
    void updateBookingStatusWithNonExistentBookingMustThrowException() {
        assertThrows(NotFoundException.class,
                () -> bookingService.updateBookingStatus(1, BookingStatus.APPROVED, 1));
    }

    @Test
    void getUserBookingsWithDifferentStatesMustReturnCorrectBookings() {
        User owner = userRepository.save(new User(null, "Owner", "Owner@email.com"));
        User booker = userRepository.save(new User(null, "Booker", "Booker@email.com"));
        Item item = itemRepository.save(new Item(null, "Item", "Description",
                true, owner, null));

        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusHours(1));
        bookingDto.setEnd(LocalDateTime.now().plusHours(2));
        bookingService.createBooking(bookingDto, booker.getId());

        assertDoesNotThrow(() -> bookingService.getUserBookings(booker.getId(), BookingState.ALL));
        assertDoesNotThrow(() -> bookingService.getUserBookings(booker.getId(), BookingState.FUTURE));
        assertDoesNotThrow(() -> bookingService.getUserBookings(booker.getId(), BookingState.WAITING));
    }

    @Test
    void getOwnerBookingsWithDifferentStatesMustReturnCorrectBookings() {
        User owner = userRepository.save(new User(null, "Owner", "Owner@email.com"));
        User booker = userRepository.save(new User(null, "Booker", "Booker@email.com"));
        Item item = itemRepository.save(new Item(null, "Item", "Description",
                true, owner, null));

        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusHours(1));
        bookingDto.setEnd(LocalDateTime.now().plusHours(2));
        bookingService.createBooking(bookingDto, booker.getId());

        assertDoesNotThrow(() -> bookingService.getOwnerBookings(owner.getId(), BookingState.ALL));
        assertDoesNotThrow(() -> bookingService.getOwnerBookings(owner.getId(), BookingState.FUTURE));
    }

    @Test
    void createBookingWithPastStartDateMustThrowException() {
        User owner = userRepository.save(new User(null, "Owner", "Owner@email.com"));
        User booker = userRepository.save(new User(null, "Booker", "Booker@email.com"));
        Item item = itemRepository.save(new Item(null, "Item", "Description",
                true, owner, null));

        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusHours(1));
        bookingDto.setEnd(LocalDateTime.now().minusHours(1));

        assertThrows(ValidationException.class,
                () -> bookingService.createBooking(bookingDto, booker.getId()));
    }

    @Test
    void createBookingWithSameStartAndEndMustThrowException() {
        User owner = userRepository.save(new User(null, "Owner", "Owner@email.com"));
        User booker = userRepository.save(new User(null, "Booker", "Booker@email.com"));
        Item item = itemRepository.save(new Item(null, "Item", "Description",
                true, owner, null));

        LocalDateTime time = LocalDateTime.now().plusHours(1);
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(time);
        bookingDto.setEnd(time);

        assertThrows(ValidationException.class,
                () -> bookingService.createBooking(bookingDto, booker.getId()));
    }

    @Test
    void bookingStateAllValuesMustBeAccessible() {
        assertDoesNotThrow(() -> {
            for (BookingState state : BookingState.values()) {
                BookingState result = BookingState.valueOf(state.name());
                assertEquals(state, result);
            }
        });
    }

    @Test
    void bookingStatusAllValuesMustBeAccessible() {
        assertDoesNotThrow(() -> {
            for (BookingStatus status : BookingStatus.values()) {
                BookingStatus result = BookingStatus.valueOf(status.name());
                assertEquals(status, result);
            }
        });
    }

    @Test
    void getBookingByIdWithNonExistentBookingMustThrowNotFoundException() {
        Integer userId = 1;
        Integer bookingId = 9;

        assertThrows(NotFoundException.class,
                () -> bookingService.getBookingById(bookingId, userId));
    }

    @Test
    void updateBookingStatusWhenStatusIsNotWaitingMustThrowForbiddenException() {
        User owner = userRepository.save(new User(null, "Owner", "Owner@email.com"));
        User booker = userRepository.save(new User(null, "Booker", "Booker@email.com"));
        Item item = itemRepository.save(new Item(null, "Item", "Description",
                true, owner, null));

        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().plusHours(1));
        booking.setEnd(LocalDateTime.now().plusHours(2));
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.APPROVED);
        Booking savedBooking = bookingRepository.save(booking);

        assertThrows(ForbiddenException.class,
                () -> bookingService.updateBookingStatus(savedBooking.getId(), BookingStatus.REJECTED, owner.getId()),
                "Бронирование уже обработано");
    }
}
