package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ItemRequestDto {
    private Integer id;
    @NotBlank
    private String description;
    private Integer requestor;
    private LocalDateTime created;
    private List<ItemDto> items;
}
