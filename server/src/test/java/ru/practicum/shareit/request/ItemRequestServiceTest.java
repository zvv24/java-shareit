package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ItemRequestServiceTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemService itemService;

    @Test
    void createRequestMustCreateRequestSuccessfully() {
        User user = userRepository.save(new User(null, "User", "User@email.com"));
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Description");

        ItemRequestDto result = itemRequestService.createRequest(user.getId(), requestDto);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Description", result.getDescription());
        assertEquals(user.getId(), result.getRequestor());
        assertNotNull(result.getCreated());
        assertNotNull(result.getItems());
    }

    @Test
    void createRequestWithNonExistentUserMustThrowException() {
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Description");

        assertThrows(NotFoundException.class,
                () -> itemRequestService.createRequest(1, requestDto));
    }

    @Test
    void getRequestsMustReturnUserRequests() {
        User user = userRepository.save(new User(null, "User", "User@email.com"));

        ItemRequestDto request1 = new ItemRequestDto();
        request1.setDescription("Description1");
        itemRequestService.createRequest(user.getId(), request1);

        ItemRequestDto request2 = new ItemRequestDto();
        request2.setDescription("Description2");
        itemRequestService.createRequest(user.getId(), request2);

        List<ItemRequestDto> result = itemRequestService.getRequests(user.getId());

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(r -> r.getDescription().equals("Description1")));
        assertTrue(result.stream().anyMatch(r -> r.getDescription().equals("Description2")));
    }

    @Test
    void getUserRequestsMustReturnOtherUsersRequests() {
        User user1 = userRepository.save(new User(null, "User 1", "User1@email.com"));
        User user2 = userRepository.save(new User(null, "User 2", "User2@email.com"));

        ItemRequestDto request1 = new ItemRequestDto();
        request1.setDescription("User1 Description");
        itemRequestService.createRequest(user1.getId(), request1);

        ItemRequestDto request2 = new ItemRequestDto();
        request2.setDescription("User2 Description");
        itemRequestService.createRequest(user2.getId(), request2);

        List<ItemRequestDto> result = itemRequestService.getUserRequests(user1.getId());

        assertFalse(result.isEmpty());
        assertTrue(result.stream().anyMatch(r -> r.getDescription().equals("User2 Description")));
        assertFalse(result.stream().anyMatch(r -> r.getDescription().equals("User1 Description")));
    }

    @Test
    void getRequestByIdMustReturnRequestWithItems() {
        User user = userRepository.save(new User(null, "User", "User@email.com"));
        User owner = userRepository.save(new User(null, "Owner", "Owner@email.com"));

        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("new item");
        ItemRequestDto savedRequest = itemRequestService.createRequest(user.getId(), requestDto);

        ItemDto itemDto = new ItemDto();
        itemDto.setName("item");
        itemDto.setDescription("new item");
        itemDto.setAvailable(true);
        itemDto.setRequestId(savedRequest.getId());
        itemService.createItem(itemDto, owner.getId());

        ItemRequestDto result = itemRequestService.getRequestById(savedRequest.getId());

        assertNotNull(result);
        assertEquals(savedRequest.getId(), result.getId());
        assertEquals("new item", result.getDescription());
        assertFalse(result.getItems().isEmpty());
        assertEquals("item", result.getItems().get(0).getName());
    }

    @Test
    void getRequestByIdWithNonExistentRequestMustThrowException() {
        assertThrows(NotFoundException.class, () -> itemRequestService.getRequestById(1));
    }

    @Test
    void getRequestsWithNonExistentUserMustThrowException() {
        assertThrows(NotFoundException.class, () -> itemRequestService.getRequests(1));
    }

    @Test
    void getRequestByIdWithItemsMustReturnRequestWithItems() {
        User user = userRepository.save(new User(null, "User", "User@email.com"));
        User owner = userRepository.save(new User(null, "Owner", "Owner@email.com"));

        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Text");
        ItemRequestDto savedRequest = itemRequestService.createRequest(user.getId(), requestDto);

        ItemDto itemDto = new ItemDto();
        itemDto.setName("Item");
        itemDto.setDescription("Description");
        itemDto.setAvailable(true);
        itemDto.setRequestId(savedRequest.getId());
        itemService.createItem(itemDto, owner.getId());

        ItemRequestDto result = itemRequestService.getRequestById(savedRequest.getId());

        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals("Item", result.getItems().get(0).getName());
    }
}
