package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ItemServiceTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    void getOwnersItemMustReturnOwnerItems() {
        User owner = userRepository.save(new User(null, "Owner", "Owner@email.com"));

        ItemDto itemDto1 = new ItemDto();
        itemDto1.setName("Item1");
        itemDto1.setDescription("Description1");
        itemDto1.setAvailable(true);
        itemService.createItem(itemDto1, owner.getId());

        ItemDto itemDto2 = new ItemDto();
        itemDto2.setName("Item2");
        itemDto2.setDescription("Description2");
        itemDto2.setAvailable(true);
        itemService.createItem(itemDto2, owner.getId());

        List<Item> result = itemService.getOwnersItem(owner.getId());

        assertEquals(2, result.size());
    }

    @Test
    void searchingItemsMustReturnAvailableItems() {
        User owner = userRepository.save(new User(null, "Owner", "Owner@email.com"));

        ItemDto itemDto1 = new ItemDto();
        itemDto1.setName("Item1");
        itemDto1.setDescription("Description1 new item1");
        itemDto1.setAvailable(true);
        itemService.createItem(itemDto1, owner.getId());

        ItemDto itemDto2 = new ItemDto();
        itemDto2.setName("Item2");
        itemDto2.setDescription("Description2");
        itemDto2.setAvailable(true);
        itemService.createItem(itemDto2, owner.getId());

        List<Item> result = itemService.searchingItems("item1");

        assertEquals(1, result.size());
        assertTrue(result.getFirst().getName().toLowerCase().contains("item1") ||
                result.getFirst().getDescription().toLowerCase().contains("item1"));
    }

    @Test
    void addCommentMustThrowExceptionWhenNoBooking() {
        User owner = userRepository.save(new User(null, "Owner", "Owner@email.com"));
        User author = userRepository.save(new User(null, "Author", "Author@email.com"));

        ItemDto itemDto = new ItemDto();
        itemDto.setName("Item");
        itemDto.setDescription("Description");
        itemDto.setAvailable(true);
        Item item = itemService.createItem(itemDto, owner.getId());

        CommentDto commentDto = new CommentDto();
        commentDto.setText("Great item!");

        assertThrows(ValidationException.class,
                () -> itemService.addComment(item.getId(), commentDto, author.getId()));
    }

    @Test
    void getItemByIdWithNonExistentItemMustThrowException() {
        assertThrows(NotFoundException.class, () -> itemService.getItemById(1, 1));
    }

    @Test
    void getItemByIdForNonOwnerMustNotIncludeBookingInfo() {
        User owner = userRepository.save(new User(null, "Owner", "Owner@email.com"));
        User user = userRepository.save(new User(null, "User", "User@email.com"));
        Item item = itemRepository.save(new Item(null, "Item", "Description", true, owner, null));

        ItemDto result = itemService.getItemById(item.getId(), user.getId());

        assertNotNull(result);
        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
    }

    @Test
    void searchingItemsWithBlankTextMustReturnEmptyList() {
        List<Item> result = itemService.searchingItems(" ");

        assertTrue(result.isEmpty());
    }

    @Test
    void updateItemWithNonExistentItemMustThrowException() {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Item");

        assertThrows(NotFoundException.class, () -> itemService.updateItem(itemDto, 5, 1));
    }

    @Test
    void updateItemWithWrongOwnerMustThrowException() {
        User owner = userRepository.save(new User(null, "Owner", "Owner@email.com"));
        User user = userRepository.save(new User(null, "User", "User@email.com"));

        ItemDto itemDto = new ItemDto();
        itemDto.setName("Item");
        itemDto.setDescription("Description");
        itemDto.setAvailable(true);
        Item item = itemService.createItem(itemDto, owner.getId());

        ItemDto updateDto = new ItemDto();
        updateDto.setName("New User");

        assertThrows(ForbiddenException.class,
                () -> itemService.updateItem(updateDto, item.getId(), user.getId()));
    }

    @Test
    void updateItemWithOnlyNameMustUpdateOnlyName() {
        User owner = userRepository.save(new User(null, "Owner", "Owner@email.com"));
        Item item = itemRepository.save(new Item(null, "Item", "Description",
                true, owner, null));

        ItemDto updateDto = new ItemDto();
        updateDto.setName("New Item");

        Item updatedItem = itemService.updateItem(updateDto, item.getId(), owner.getId());

        assertEquals("New Item", updatedItem.getName());
        assertEquals("Description", updatedItem.getDescription());
        assertTrue(updatedItem.getAvailable());
    }

    @Test
    void updateItemWithOnlyDescriptionMustUpdateOnlyDescription() {
        User owner = userRepository.save(new User(null, "Owner", "Owner@email.com"));
        Item item = itemRepository.save(new Item(null, "Item", "Description",
                true, owner, null));

        ItemDto updateDto = new ItemDto();
        updateDto.setDescription("New Description");

        Item updatedItem = itemService.updateItem(updateDto, item.getId(), owner.getId());

        assertEquals("Item", updatedItem.getName());
        assertEquals("New Description", updatedItem.getDescription());
        assertTrue(updatedItem.getAvailable());
    }

    @Test
    void updateItemWithOnlyAvailableMustUpdateOnlyAvailable() {
        User owner = userRepository.save(new User(null, "Owner", "Owner@email.com"));
        Item item = itemRepository.save(new Item(null, "Item", "Description",
                true, owner, null));

        ItemDto updateDto = new ItemDto();
        updateDto.setAvailable(false);

        Item updatedItem = itemService.updateItem(updateDto, item.getId(), owner.getId());

        assertEquals("Item", updatedItem.getName());
        assertEquals("Description", updatedItem.getDescription());
        assertFalse(updatedItem.getAvailable());
    }

    @Test
    void addCommentWithValidBookingMustAddComment() {
        User owner = userRepository.save(new User(null, "Owner", "Owner@email.com"));
        User author = userRepository.save(new User(null, "Author", "Author@email.com"));
        Item item = itemRepository.save(new Item(null, "Item", "Description",
                true, owner, null));

        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().minusHours(2));
        booking.setEnd(LocalDateTime.now().minusHours(1));
        booking.setItem(item);
        booking.setBooker(author);
        booking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(booking);

        CommentDto commentDto = new CommentDto();
        commentDto.setText("Text");

        CommentDto result = itemService.addComment(item.getId(), commentDto, author.getId());

        assertNotNull(result);
        assertEquals("Text", result.getText());
    }

    @Test
    void createItemWithInvalidDataMustReturnBadRequest() {
        ItemDto itemDto = new ItemDto();

        assertThrows(NotFoundException.class,
                () -> itemService.createItem(itemDto, 9));
    }

    @Test
    void getItemByIdForOwnerWithPastAndFutureBookingsMustIncludeBoth() {
        User owner = userRepository.save(new User(null, "Owner", "Owner@email.com"));
        User booker1 = userRepository.save(new User(null, "Booker1", "Booker1@email.com"));
        User booker2 = userRepository.save(new User(null, "Booker2", "Booker2@email.com"));
        Item item = itemRepository.save(new Item(null, "Item", "Description", true, owner, null));

        Booking pastBooking = new Booking();
        pastBooking.setStart(LocalDateTime.now().minusHours(3));
        pastBooking.setEnd(LocalDateTime.now().minusHours(1));
        pastBooking.setItem(item);
        pastBooking.setBooker(booker1);
        pastBooking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(pastBooking);

        Booking futureBooking = new Booking();
        futureBooking.setStart(LocalDateTime.now().plusHours(1));
        futureBooking.setEnd(LocalDateTime.now().plusHours(2));
        futureBooking.setItem(item);
        futureBooking.setBooker(booker2);
        futureBooking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(futureBooking);

        ItemDto result = itemService.getItemById(item.getId(), owner.getId());

        assertNotNull(result.getLastBooking());
        assertEquals(pastBooking.getId(), result.getLastBooking().getId());
        assertEquals(booker1.getId(), result.getLastBooking().getBookerId());

        assertNotNull(result.getNextBooking());
        assertEquals(futureBooking.getId(), result.getNextBooking().getId());
        assertEquals(booker2.getId(), result.getNextBooking().getBookerId());
    }
}