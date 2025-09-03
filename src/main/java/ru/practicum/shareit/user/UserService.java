package ru.practicum.shareit.user;

import java.util.List;

public interface UserService {
    User createUser(User user);

    User updateUser(Integer id, User user);

    User getUserById(Integer id);

    List<User> getAllUsers();

    void deleteUser(Integer id);
}
