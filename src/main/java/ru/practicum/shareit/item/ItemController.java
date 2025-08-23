package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;
    private final UserService userService;
    private static final String HEAD = "X-Sharer-User-Id";

    @PostMapping
    public ItemDto createItem(@Valid @RequestBody ItemDto itemDto, @RequestHeader(HEAD) Integer ownerId) {
        User owner = userService.getUserById(ownerId);
        return ItemMapper.mapToItemDto(itemService.createItem(itemDto, owner.getId()));
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestBody ItemDto itemDto, @PathVariable Integer itemId,
                              @RequestHeader(HEAD) Integer ownerId) {
        return ItemMapper.mapToItemDto(itemService.updateItem(itemDto, itemId, ownerId));
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@PathVariable Integer itemId) {
        return ItemMapper.mapToItemDto(itemService.getItemById(itemId));
    }

    @GetMapping
    public List<ItemDto> getOwnersItem(@RequestHeader(HEAD) Integer ownerId) {
        return itemService.getOwnersItem(ownerId).stream()
                .map(ItemMapper::mapToItemDto)
                .toList();
    }

    @GetMapping("/search")
    public List<ItemDto> searchingItems(@RequestParam String text) {
        return itemService.searchingItems(text).stream()
                .map(ItemMapper::mapToItemDto)
                .toList();
    }
}
