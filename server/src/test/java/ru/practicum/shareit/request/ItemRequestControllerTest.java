package ru.practicum.shareit.request;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.util.Constants;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService itemRequestService;

    @Test
    void createRequestMustReturnRequest() throws Exception {
        Integer userId = 1;
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Description");
        ItemRequestDto savedRequest = new ItemRequestDto();
        savedRequest.setId(1);
        savedRequest.setDescription("Description");
        savedRequest.setRequestor(userId);
        savedRequest.setCreated(LocalDateTime.now());

        when(itemRequestService.createRequest(eq(userId), any(ItemRequestDto.class)))
                .thenReturn(savedRequest);

        mockMvc.perform(post("/requests")
                        .header(Constants.HEAD, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Description"));
    }

    @Test
    void createRequestWithInvalidUserMustReturnError() throws Exception {
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Description");

        when(itemRequestService.createRequest(any(Integer.class), any(ItemRequestDto.class)))
                .thenThrow(new NotFoundException("Пользователь не найден"));

        mockMvc.perform(post("/requests")
                        .header(Constants.HEAD, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getRequestByIdWithNonExistentRequestMustReturnError() throws Exception {
        when(itemRequestService.getRequestById(any(Integer.class)))
                .thenThrow(new NotFoundException("Запрос не найден"));

        mockMvc.perform(get("/requests/{requestId}", 1))
                .andExpect(status().isNotFound());
    }

    @Test
    void getRequestsMustReturnRequests() throws Exception {
        Integer userId = 1;
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setId(1);
        requestDto.setDescription("Description");
        List<ItemRequestDto> requests = List.of(requestDto);

        when(itemRequestService.getRequests(eq(userId)))
                .thenReturn(requests);

        mockMvc.perform(get("/requests")
                        .header(Constants.HEAD, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getRequestByIdMustReturnRequest() throws Exception {
        Integer requestId = 1;
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setId(1);
        requestDto.setDescription("Description");

        when(itemRequestService.getRequestById(eq(requestId)))
                .thenReturn(requestDto);

        mockMvc.perform(get("/requests/{requestId}", requestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getUserRequestsMustReturnRequests() throws Exception {
        Integer userId = 1;
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setId(1);
        requestDto.setDescription("Description");
        List<ItemRequestDto> requests = List.of(requestDto);

        when(itemRequestService.getUserRequests(eq(userId)))
                .thenReturn(requests);

        mockMvc.perform(get("/requests/all")
                        .header(Constants.HEAD, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }
}
