package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ItemServiceTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserRepository userRepository;

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
}