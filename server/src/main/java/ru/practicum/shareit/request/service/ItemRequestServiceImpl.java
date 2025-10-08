package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public ItemRequestDto createRequest(Integer userId, ItemRequestDto itemRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        ItemRequest itemRequest = ItemRequestMapper.mapToItemRequest(itemRequestDto, user);
        ItemRequest savedRequest = itemRequestRepository.save(itemRequest);

        return ItemRequestMapper.mapToItemRequestDto(savedRequest, Collections.emptyList());
    }

    @Override
    public List<ItemRequestDto> getRequests(Integer userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        List<ItemRequest> requests = itemRequestRepository.findByRequestorIdOrderByCreatedDesc(userId);

        List<Integer> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .toList();

        List<Item> itemList = itemRepository.findByRequestIdIn(requestIds);
        Map<Integer, List<ItemDto>> itemsMap = itemList.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getRequest().getId(),
                        Collectors.mapping(ItemMapper::mapToItemDto, Collectors.toList())
                ));

        return requests.stream()
                .map(request -> {
                    List<ItemDto> items = itemsMap.getOrDefault(request.getId(), Collections.emptyList());
                    return ItemRequestMapper.mapToItemRequestDto(request, items);
                })
                .toList();
    }

    @Override
    public List<ItemRequestDto> getUserRequests(Integer userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        List<ItemRequest> requests = itemRequestRepository.findByRequestorIdNotOrderByCreatedDesc(userId);

        List<Integer> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());

        List<Item> itemList = itemRepository.findByRequestIdIn(requestIds);
        Map<Integer, List<ItemDto>> itemsMap = itemList.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getRequest().getId(),
                        Collectors.mapping(ItemMapper::mapToItemDto, Collectors.toList())
                ));

        return requests.stream()
                .map(request -> {
                    List<ItemDto> items = itemsMap.getOrDefault(request.getId(), Collections.emptyList());
                    return ItemRequestMapper.mapToItemRequestDto(request, items);
                })
                .toList();
    }

    @Override
    public ItemRequestDto getRequestById(Integer requestId) {
        ItemRequest request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос не найден"));

        List<ItemDto> items = getItem(request.getId());
        return ItemRequestMapper.mapToItemRequestDto(request, items);
    }

    private List<ItemDto> getItem(Integer id) {
        return itemRepository.findByRequestId(id).stream()
                .map(ItemMapper::mapToItemDto)
                .toList();
    }
}
