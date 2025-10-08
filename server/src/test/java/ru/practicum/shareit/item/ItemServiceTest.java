package ru.practicum.shareit.item;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ItemServiceTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private EntityManager entityManager;

    private User owner;
    private User booker;
    private User user;
    private Item item;

    @BeforeEach
    void setup() {
        owner = createAndSaveUser("Owner", "Owner@email.com");
        user = createAndSaveUser("User", "User@email.com");
        booker = createAndSaveUser("Booker", "Booker@email.com");
        item = createAndSaveItem("Item", "Description", owner, true);
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
    void getOwnersItemMustReturnOwnerItems() {
        ItemDto itemDto1 = new ItemDto();
        itemDto1.setName("Item1");
        itemDto1.setDescription("Description1");
        itemDto1.setAvailable(true);
        itemService.createItem(itemDto1, owner.getId());

        List<Item> result = itemService.getOwnersItem(owner.getId());

        assertEquals(2, result.size());
    }

    @Test
    void searchingItemsMustReturnAvailableItems() {
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
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Item");
        itemDto.setDescription("Description");
        itemDto.setAvailable(true);
        Item item = itemService.createItem(itemDto, owner.getId());

        CommentDto commentDto = new CommentDto();
        commentDto.setText("Text");

        assertThrows(ValidationException.class,
                () -> itemService.addComment(item.getId(), commentDto, user.getId()));
    }

    @Test
    void getItemByIdWithNonExistentItemMustThrowException() {
        assertThrows(NotFoundException.class, () -> itemService.getItemById(1, 1));
    }

    @Test
    void getItemByIdForNonOwnerMustNotIncludeBookingInfo() {
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
        ItemDto updateDto = new ItemDto();
        updateDto.setName("New Item");

        Item updatedItem = itemService.updateItem(updateDto, item.getId(), owner.getId());

        assertEquals("New Item", updatedItem.getName());
        assertEquals("Description", updatedItem.getDescription());
        assertTrue(updatedItem.getAvailable());
    }

    @Test
    void updateItemWithOnlyDescriptionMustUpdateOnlyDescription() {
        ItemDto updateDto = new ItemDto();
        updateDto.setDescription("New Description");

        Item updatedItem = itemService.updateItem(updateDto, item.getId(), owner.getId());

        assertEquals("Item", updatedItem.getName());
        assertEquals("New Description", updatedItem.getDescription());
        assertTrue(updatedItem.getAvailable());
    }

    @Test
    void updateItemWithOnlyAvailableMustUpdateOnlyAvailable() {
        ItemDto updateDto = new ItemDto();
        updateDto.setAvailable(false);

        Item updatedItem = itemService.updateItem(updateDto, item.getId(), owner.getId());

        assertEquals("Item", updatedItem.getName());
        assertEquals("Description", updatedItem.getDescription());
        assertFalse(updatedItem.getAvailable());
    }

    @Test
    void addCommentWithValidBookingMustAddComment() {
        createAndSaveBooking(
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now().minusHours(1),
                item, booker, BookingStatus.APPROVED
        );

        CommentDto commentDto = new CommentDto();
        commentDto.setText("Text");

        CommentDto result = itemService.addComment(item.getId(), commentDto, booker.getId());

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
        User booker1 = createAndSaveUser("Booker1", "Booker1@email.com");
        User booker2 = createAndSaveUser("Booker2", "Booker2@email.com");

        Booking pastBooking = createAndSaveBooking(
                LocalDateTime.now().minusHours(3),
                LocalDateTime.now().minusHours(1),
                item, booker1, BookingStatus.APPROVED
        );

        Booking futureBooking = createAndSaveBooking(
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2),
                item, booker2, BookingStatus.APPROVED
        );

        ItemDto result = itemService.getItemById(item.getId(), owner.getId());

        assertNotNull(result.getLastBooking());
        assertEquals(pastBooking.getId(), result.getLastBooking().getId());
        assertEquals(booker1.getId(), result.getLastBooking().getBookerId());

        assertNotNull(result.getNextBooking());
        assertEquals(futureBooking.getId(), result.getNextBooking().getId());
        assertEquals(booker2.getId(), result.getNextBooking().getBookerId());
    }
}