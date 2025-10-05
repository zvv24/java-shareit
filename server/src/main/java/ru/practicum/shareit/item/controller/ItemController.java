package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.util.Constants;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ItemDto createItem(@Valid @RequestBody ItemDto itemDto, @RequestHeader(Constants.HEAD) Integer ownerId) {
        Item item = itemService.createItem(itemDto, ownerId);
        return ItemMapper.mapToItemDto(item);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestBody ItemDto itemDto, @PathVariable Integer itemId,
                              @RequestHeader(Constants.HEAD) Integer ownerId) {
        return ItemMapper.mapToItemDto(itemService.updateItem(itemDto, itemId, ownerId));
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@PathVariable Integer itemId,
                               @RequestHeader(Constants.HEAD) Integer userId) {
        return itemService.getItemById(itemId, userId);
    }

    @GetMapping
    public List<ItemDto> getOwnersItem(@RequestHeader(Constants.HEAD) Integer ownerId) {
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

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@PathVariable Integer itemId,
                                 @Valid @RequestBody CommentDto commentDto,
                                 @RequestHeader(Constants.HEAD) Integer authorId) {
        return itemService.addComment(itemId, commentDto, authorId);
    }
}
