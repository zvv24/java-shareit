package ru.practicum.shareit.user;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void createUserMustCreateUserSuccessfully() {
        User user = new User(null, "User", "User@email.com");

        User result = userService.createUser(user);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("User", result.getName());
        assertEquals("User@email.com", result.getEmail());
    }

    @Test
    void createUserWithDuplicateEmailMustThrowException() {
        User user1 = new User(null, "User", "User@email.com");
        userService.createUser(user1);

        User user2 = new User(null, "User1", "User@email.com");

        assertThrows(ConflictException.class, () -> userService.createUser(user2));
    }

    @Test
    void createUserWithInvalidEmailMustThrowException() {
        User user = new User(null, "User", "user");

        assertThrows(ru.practicum.shareit.exception.ValidationException.class, () -> userService.createUser(user));
    }

    @Test
    void updateUserMustUpdateUserSuccessfully() {
        User user = userRepository.save(new User(null, "User", "User@email.com"));
        User newUser = new User(null, "NewUser", "NewUser@email.com");

        User result = userService.updateUser(user.getId(), newUser);

        assertEquals("NewUser", result.getName());
        assertEquals("NewUser@email.com", result.getEmail());
    }

    @Test
    void updateUserWithNonExistentUserMustThrowException() {
        User user = new User(null, "User", null);

        assertThrows(NotFoundException.class,
                () -> userService.updateUser(1, user));
    }

    @Test
    void getUserByIdMustReturnUser() {
        User user = userRepository.save(new User(null, "User", "User@email.com"));

        User result = userService.getUserById(user.getId());

        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals("User", result.getName());
        assertEquals("User@email.com", result.getEmail());
    }

    @Test
    void getAllUsersMustReturnAllUsers() {
        userRepository.save(new User(null, "User1", "User1@email.com"));
        userRepository.save(new User(null, "User2", "User2@email.com"));

        List<User> result = userService.getAllUsers();

        assertFalse(result.isEmpty());
        assertTrue(result.size() >= 2);
    }

    @Test
    void deleteUserMustDeleteUserSuccessfully() {
        User user = userRepository.save(new User(null, "User", "User@email.com"));

        userService.deleteUser(user.getId());

        assertThrows(NotFoundException.class, () -> userService.getUserById(user.getId()));
    }

    @Test
    void getUserByIdWithNonExistentUserMustThrowException() {
        assertThrows(NotFoundException.class, () -> userService.getUserById(5));
    }

    @Test
    void getUserByIdWithNullUserIdMustThrowException() {
        assertThrows(InvalidDataAccessApiUsageException.class, () -> userService.getUserById(null));
    }

    @Test
    void updateUserPartialUpdateMustUpdateOnlyProvidedFields() {
        User savedUser = userRepository.save(new User(null, "User", "User@email.com"));
        User updateData = new User(null, "NewUser", null);

        User result = userService.updateUser(savedUser.getId(), updateData);

        assertEquals("NewUser", result.getName());
        assertEquals("User@email.com", result.getEmail());
    }

    @Test
    void createUserWithNullEmailMustThrowException() {
        User user = new User(null, "User", null);
        assertThrows(ValidationException.class, () -> userService.createUser(user));
    }

    @Test
    void updateUserWithOnlyEmailMustUpdateOnlyEmail() {
        User user = userRepository.save(new User(null, "User", "User@email.com"));
        User newUser = new User(null, null, "newUser@email.com");

        User result = userService.updateUser(user.getId(), newUser);

        assertEquals("User", result.getName());
        assertEquals("newUser@email.com", result.getEmail());
    }

    @Test
    void updateUserWithDuplicateEmailMustThrowException() {
        User user1 = userRepository.save(new User(null, "User1", "User1@email.com"));
        User user2 = userRepository.save(new User(null, "User2", "User2@email.com"));

        User updateData = new User(null, "User2", "User1@email.com");

        assertThrows(ConflictException.class,
                () -> userService.updateUser(user2.getId(), updateData));
    }
}
