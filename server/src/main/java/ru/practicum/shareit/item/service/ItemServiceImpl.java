package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.mapper.CommentMapper;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.comment.repository.CommentRepository;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    public Item createItem(ItemDto itemDto, Integer ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Item item = ItemMapper.mapToItem(itemDto, ownerId);
        item.setOwner(owner);
        if (itemDto.getRequestId() != null) {
            ItemRequest request = itemRequestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Запрос не найден"));
            item.setRequest(request);
        }
        return itemRepository.save(item);
    }

    @Override
    public Item updateItem(ItemDto itemDto, Integer itemId, Integer ownerId) {
        Item newItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));
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
        return itemRepository.save(newItem);
    }

    @Override
    public ItemDto getItemById(Integer itemId, Integer userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        ItemDto itemDto = ItemMapper.mapToItemDto(item);
        if (item.getOwner().getId().equals(userId)) {
            addBookingInfoToItemDto(itemDto, itemId);
        }
        addCommentsToItemDto(itemDto, itemId);

        return itemDto;
    }

    @Override
    public List<Item> getOwnersItem(Integer ownerId) {
        return itemRepository.findByOwnerIdOrderById(ownerId);
    }

    @Override
    public List<Item> searchingItems(String text) {
        if (text.isBlank()) {
            return List.of();
        }

        return itemRepository.searchAvailableItems(text);
    }

    @Override
    public CommentDto addComment(Integer itemId, CommentDto commentDto, Integer authorId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        User user = userRepository.findById(authorId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        boolean hasBooked = bookingRepository.existsByItemIdAndBookerIdAndEndBefore(
                itemId, authorId, LocalDateTime.now());

        if (!hasBooked) {
            throw new ValidationException("Только пользователи, бравшие вещь в аренду, могут оставлять комментарии");
        }

        Comment comment = CommentMapper.mapToComment(commentDto, item, user);
        comment.setCreated(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);
        return CommentMapper.mapToCommentDto(savedComment);
    }

    private void addBookingInfoToItemDto(ItemDto itemDto, Integer itemId) {
        LocalDateTime now = LocalDateTime.now();

        Optional<Booking> lastBooking = bookingRepository
                .findFirstByItemIdAndEndBeforeOrderByEndDesc(itemId, now);
        lastBooking.ifPresent(booking -> {
            ItemDto.BookingShortDto dto = new ItemDto.BookingShortDto();
            dto.setId(booking.getId());
            dto.setBookerId(booking.getBooker().getId());
            itemDto.setLastBooking(dto);
        });

        Optional<Booking> nextBooking = bookingRepository
                .findFirstByItemIdAndStartAfterOrderByStartAsc(itemId, now);
        nextBooking.ifPresent(booking -> {
            ItemDto.BookingShortDto dto = new ItemDto.BookingShortDto();
            dto.setId(booking.getId());
            dto.setBookerId(booking.getBooker().getId());
            itemDto.setNextBooking(dto);
        });
    }

    private void addCommentsToItemDto(ItemDto itemDto, Integer itemId) {
        List<Comment> comments = commentRepository.findByItemId(itemId);
        List<CommentDto> commentDto = comments.stream()
                .sorted(Comparator.comparing(Comment::getCreated).reversed())
                .map(CommentMapper::mapToCommentDto)
                .collect(Collectors.toList());
        itemDto.setComments(commentDto);
    }
}