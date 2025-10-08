package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    void testSerialize() throws Exception {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(1);
        itemDto.setName("Item");
        itemDto.setDescription("Description");
        itemDto.setAvailable(true);

        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setId(1);
        requestDto.setDescription("Description");
        requestDto.setRequestor(1);
        requestDto.setCreated(LocalDateTime.of(2025, 10, 5, 21, 0, 0));
        requestDto.setItems(List.of(itemDto));

        JsonContent<ItemRequestDto> result = json.write(requestDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description")
                .isEqualTo("Description");
        assertThat(result).extractingJsonPathNumberValue("$.requestor").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.created")
                .isEqualTo("2025-10-05T21:00:00");
        assertThat(result).extractingJsonPathArrayValue("$.items").hasSize(1);
        assertThat(result).extractingJsonPathNumberValue("$.items[0].id").isEqualTo(1);
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\"id\":1,\"description\":\"Description\",\"requestor\":1," +
                "\"created\":\"2025-10-05T21:00:00\",\"items\":[]}";

        ItemRequestDto result = json.parseObject(content);

        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getDescription()).isEqualTo("Description");
        assertThat(result.getRequestor()).isEqualTo(1);
        assertThat(result.getCreated()).isEqualTo(LocalDateTime.of(2025, 10, 5,
                21, 0, 0));
        assertThat(result.getItems()).isEmpty();
    }

    @Test
    void testDeserializeWithNullValues() throws Exception {
        String content = "{\"description\":\"Description\"}";

        ItemRequestDto result = json.parseObject(content);

        assertThat(result.getDescription()).isEqualTo("Description");
        assertThat(result.getId()).isNull();
        assertThat(result.getRequestor()).isNull();
        assertThat(result.getCreated()).isNull();
        assertThat(result.getItems()).isNull();
    }
}