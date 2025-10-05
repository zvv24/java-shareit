package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void createUserMustReturnUser() throws Exception {
        UserDto userDto = new UserDto(null, "User", "User@example.com");
        User user = new User(1, "User", "User@example.com");

        when(userService.createUser(any(User.class)))
                .thenReturn(user);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("User"))
                .andExpect(jsonPath("$.email").value("User@example.com"));
    }

    @Test
    void updateUserMustReturnUpdatedUser() throws Exception {
        Integer userId = 1;
        UserDto userDto = new UserDto(null, "UserDto", "UserDto@example.com");
        User user = new User(1, "User", "User@example.com");

        when(userService.updateUser(eq(userId), any(User.class)))
                .thenReturn(user);

        mockMvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("User"))
                .andExpect(jsonPath("$.email").value("User@example.com"));
    }

    @Test
    void getAllUsersMustReturnUsers() throws Exception {
        List<User> users = List.of(
                new User(1, "User1", "User1@example.com"),
                new User(2, "User2", "User2@example.com")
        );

        when(userService.getAllUsers())
                .thenReturn(users);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("User1"))
                .andExpect(jsonPath("$[1].name").value("User2"));
    }

    @Test
    void getUserByIdMustReturnUser() throws Exception {
        Integer userId = 1;
        User user = new User(1, "User", "User@example.com");

        when(userService.getUserById(eq(userId)))
                .thenReturn(user);

        mockMvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("User"))
                .andExpect(jsonPath("$.email").value("User@example.com"));
    }

    @Test
    void deleteUserMustReturnNoContent() throws Exception {
        Integer userId = 1;

        mockMvc.perform(delete("/users/{userId}", userId))
                .andExpect(status().isOk());
    }
}
