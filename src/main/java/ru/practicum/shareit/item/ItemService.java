package ru.practicum.shareit.item;

import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    Item createItem(ItemDto itemDto, Integer ownerId);

    Item updateItem(ItemDto itemDto, Integer itemId, Integer ownerId);

    ItemDto getItemById(Integer itemId, Integer userId);

    List<Item> getOwnersItem(Integer ownerId);

    List<Item> searchingItems(String text);

    CommentDto addComment(Integer itemId, CommentDto commentDto, Integer authorId);
}
