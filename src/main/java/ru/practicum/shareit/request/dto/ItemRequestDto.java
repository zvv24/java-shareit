package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-item-requests.
 */
@Data
public class ItemRequestDto {
    private Integer id;
    @NotBlank
    private String description;
    @NotBlank
    private User requestor;
    @NotBlank
    private LocalDateTime created;
}
