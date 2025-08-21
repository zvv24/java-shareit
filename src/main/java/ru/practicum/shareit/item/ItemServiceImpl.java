package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final Map<Integer, Item> items = new HashMap<>();
    private Integer idCounter = 1;
    private final UserService userService;


    @Override
    public Item createItem(ItemDto itemDto, Integer ownerId) {
        Item item = ItemMapper.mapToItem(itemDto, userService.getUserById(ownerId));
        item.setId(idCounter++);
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item updateItem(ItemDto itemDto, Integer itemId, Integer ownerId) {
        Item newItem = items.get(itemId);
        if (!newItem.getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("Только владелец может обновлять информацию");
        }
        if (itemDto.getName() != null) {
            newItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            newItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            newItem.setAvailable(itemDto.getAvailable());
        }
        return newItem;
    }

    @Override
    public Item getItemById(Integer itemId) {
        return items.get(itemId);
    }

    @Override
    public List<Item> getOwnersItem(Integer ownerId) {
        return items.values().stream().filter(item -> item.getOwner().getId().equals(ownerId)).toList();
    }

    @Override
    public List<Item> searchingItems(String text) {
        if (text.isBlank()) {
            return List.of();
        }

        return items.values().stream().filter(item -> Boolean.TRUE.equals(item.getAvailable()) && (item.getName().toLowerCase().contains(text.toLowerCase())) || item.getDescription().toLowerCase().contains(text.toLowerCase())).toList();
    }
}
