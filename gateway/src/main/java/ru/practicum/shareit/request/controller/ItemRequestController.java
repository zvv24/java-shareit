package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.client.ItemRequestClient;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.util.Constants;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Validated
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> createRequest(@RequestBody ItemRequestDto itemRequestDto,
                                                @RequestHeader(Constants.HEAD) Integer userId) {
        return itemRequestClient.createRequest(itemRequestDto, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getRequests(@RequestHeader(Constants.HEAD) Long userId) {
        return itemRequestClient.getRequests(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getUserRequests(@RequestHeader(Constants.HEAD) Integer userId) {
        return itemRequestClient.getUserRequests(userId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestById(@PathVariable Integer requestId,
                                                 @RequestHeader(Constants.HEAD) Long userId) {
        return itemRequestClient.getRequestById(requestId, userId);
    }
}