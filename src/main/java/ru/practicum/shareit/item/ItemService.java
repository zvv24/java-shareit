package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    Item createItem(ItemDto itemDto, Integer ownerId);

    Item updateItem(ItemDto itemDto, Integer itemId, Integer ownerId);

    Item getItemById(Integer itemId);

    List<Item> getOwnersItem(Integer ownerId);

    List<Item> searchingItems(String text);
}
