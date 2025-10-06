package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.util.Constants;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    @Test
    void createItemMustReturnItem() throws Exception {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Item");
        itemDto.setDescription("Description");
        itemDto.setAvailable(true);

        User owner = new User(1, "Owner", "Owner@email.com");
        Item item = new Item(1, "Item", "Description", true, owner, null);

        when(itemService.createItem(any(ItemDto.class), eq(owner.getId())))
                .thenReturn(item);

        mockMvc.perform(post("/items")
                        .header(Constants.HEAD, owner.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(item.getId()))
                .andExpect(jsonPath("$.name").value(item.getName()));
    }

    @Test
    void updateItemMustReturnUpdatedItem() throws Exception {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Item");

        User owner = new User(1, "Owner", "Owner@email.com");
        Item item = new Item(1, "Item", "Description", true, owner, null);

        when(itemService.updateItem(any(ItemDto.class), eq(item.getId()), eq(owner.getId())))
                .thenReturn(item);

        mockMvc.perform(patch("/items/{itemId}", item.getId())
                        .header(Constants.HEAD, owner.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Item"));
    }

    @Test
    void getItemByIdMustReturnItem() throws Exception {
        Integer userId = 1;
        Integer itemId = 1;

        ItemDto itemDto = new ItemDto();
        itemDto.setId(1);
        itemDto.setName("Item");
        itemDto.setDescription("Description");
        itemDto.setAvailable(true);

        when(itemService.getItemById(eq(itemId), eq(userId)))
                .thenReturn(itemDto);

        mockMvc.perform(get("/items/{itemId}", itemId)
                        .header(Constants.HEAD, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemDto.getId()));
    }

    @Test
    void getOwnersItemMustReturnItems() throws Exception {
        User owner = new User(1, "Owner", "Owner@email.com");
        Item item = new Item(1, "Item", "Description", true, owner, null);
        List<Item> items = List.of(item);

        when(itemService.getOwnersItem(eq(owner.getId())))
                .thenReturn(items);

        mockMvc.perform(get("/items")
                        .header(Constants.HEAD, owner.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(items.get(0).getId()));
    }

    @Test
    void searchingItemsMustReturnItems() throws Exception {
        User owner = new User(1, "Owner", "Owner@email.com");
        Item item = new Item(1, "Item", "Description new Item", true, owner, null);
        List<Item> items = List.of(item);

        when(itemService.searchingItems(eq("Item")))
                .thenReturn(items);

        mockMvc.perform(get("/items/search")
                        .param("text", "Item"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(items.get(0).getId()));
    }

    @Test
    void addCommentMustReturnComment() throws Exception {
        Integer itemId = 1;
        Integer authorId = 1;

        CommentDto commentDto = new CommentDto();
        commentDto.setText("Text");

        CommentDto savedCommentDto = new CommentDto(1, "Text", "Author", null);

        when(itemService.addComment(eq(itemId), any(CommentDto.class), eq(authorId)))
                .thenReturn(savedCommentDto);

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(Constants.HEAD, authorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedCommentDto.getId()))
                .andExpect(jsonPath("$.text").value(savedCommentDto.getText()));
    }

    @Test
    void getOwnersItemWithEmptyListMustReturnEmptyList() throws Exception {
        Integer id = 1;
        when(itemService.getOwnersItem(eq(id))).thenReturn(List.of());

        mockMvc.perform(get("/items")
                        .header(Constants.HEAD, id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}