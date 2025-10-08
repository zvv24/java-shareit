package ru.practicum.shareit.booking;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
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
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class BookingServiceTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private EntityManager entityManager;

    private User owner;
    private User booker;
    private Item item1;
    private Item item2;

    @BeforeEach
    void setup() {
        owner = createAndSaveUser("Owner", "Owner@email.com");
        booker = createAndSaveUser("Booker", "Booker@email.com");
        item1 = createAndSaveItem("Item 1", "Description 1", owner, true);
        item2 = createAndSaveItem("Item 2", "Description 2", owner, false);
    }

    private User createAndSaveUser(String name, String email) {
        User user = User.builder()
                .name(name)
                .email(email)
                .build();
        entityManager.persist(user);
        entityManager.flush();
        return user;
    }

    private Item createAndSaveItem(String name, String description, User owner, Boolean available) {
        Item item = Item.builder()
                .name(name)
                .description(description)
                .available(available)
                .owner(owner)
                .build();
        entityManager.persist(item);
        entityManager.flush();
        return item;
    }

    private Booking createAndSaveBooking(LocalDateTime start, LocalDateTime end, Item item, User booker, BookingStatus status) {
        Booking booking = Booking.builder()
                .start(start)
                .end(end)
                .item(item)
                .booker(booker)
                .status(status)
                .build();
        entityManager.persist(booking);
        entityManager.flush();
        return booking;
    }


    @Test
    void createBookingMustCreateReservation() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item1.getId());
        bookingDto.setStart(LocalDateTime.now().plusHours(1));
        bookingDto.setEnd(LocalDateTime.now().plusHours(2));

        BookingFullDto result = bookingService.createBooking(bookingDto, booker.getId());

        assertNotNull(result);
        assertEquals(item1.getId(), result.getItem().getId());
        assertEquals(booker.getId(), result.getBooker().getId());
    }

    @Test
    void createBookingWithUnavailableItemMustThrowException() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item2.getId());
        bookingDto.setStart(LocalDateTime.now().plusHours(1));
        bookingDto.setEnd(LocalDateTime.now().plusHours(2));

        assertThrows(ValidationException.class,
                () -> bookingService.createBooking(bookingDto, booker.getId()));
    }

    @Test
    void getUserBookingsMustReturnUserBookings() {
        createAndSaveBooking(
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2),
                item1, booker, BookingStatus.APPROVED
        );

        List<BookingFullDto> result = bookingService.getUserBookings(booker.getId(), BookingState.ALL);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void createBookingWithNonExistentItemMustThrowException() {
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
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item1.getId());
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
        User user = createAndSaveUser("User", "User@email.com");

        Booking booking = createAndSaveBooking(
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2),
                item1, booker, BookingStatus.WAITING
        );

        assertThrows(ForbiddenException.class,
                () -> bookingService.updateBookingStatus(booking.getId(), BookingStatus.APPROVED, user.getId()));
    }

    @Test
    void updateBookingStatusWithNonExistentBookingMustThrowException() {
        assertThrows(NotFoundException.class,
                () -> bookingService.updateBookingStatus(1, BookingStatus.APPROVED, 1));
    }

    @Test
    void getUserBookingsWithDifferentStatesMustReturnCorrectBookings() {
        createAndSaveBooking(
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2),
                item1, booker, BookingStatus.WAITING
        );

        assertDoesNotThrow(() -> bookingService.getUserBookings(booker.getId(), BookingState.ALL));
        assertDoesNotThrow(() -> bookingService.getUserBookings(booker.getId(), BookingState.FUTURE));
        assertDoesNotThrow(() -> bookingService.getUserBookings(booker.getId(), BookingState.WAITING));
    }

    @Test
    void getOwnerBookingsWithDifferentStatesMustReturnCorrectBookings() {
        createAndSaveBooking(
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2),
                item1, booker, BookingStatus.WAITING
        );

        assertDoesNotThrow(() -> bookingService.getOwnerBookings(owner.getId(), BookingState.ALL));
        assertDoesNotThrow(() -> bookingService.getOwnerBookings(owner.getId(), BookingState.FUTURE));
    }

    @Test
    void createBookingWithPastStartDateMustThrowException() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item1.getId());
        bookingDto.setStart(LocalDateTime.now().plusHours(1));
        bookingDto.setEnd(LocalDateTime.now().minusHours(1));

        assertThrows(ValidationException.class,
                () -> bookingService.createBooking(bookingDto, booker.getId()));
    }

    @Test
    void createBookingWithSameStartAndEndMustThrowException() {
        LocalDateTime time = LocalDateTime.now().plusHours(1);
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item1.getId());
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
        Booking booking = createAndSaveBooking(
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2),
                item1, booker, BookingStatus.APPROVED
        );

        assertThrows(ForbiddenException.class,
                () -> bookingService.updateBookingStatus(booking.getId(), BookingStatus.REJECTED, owner.getId()),
                "Бронирование уже обработано");
    }
}
