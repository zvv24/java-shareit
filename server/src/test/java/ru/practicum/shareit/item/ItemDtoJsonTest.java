package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemDtoJsonTest {

    @Autowired
    private JacksonTester<ItemDto> json;

    @Test
    void testSerialize() throws Exception {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(1);
        itemDto.setName("Item");
        itemDto.setDescription("Description");
        itemDto.setAvailable(true);
        itemDto.setOwner(1);
        itemDto.setRequestId(1);

        JsonContent<ItemDto> result = json.write(itemDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Item");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Description");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(true);
        assertThat(result).extractingJsonPathNumberValue("$.owner").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(1);
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\"id\":1,\"name\":\"Item\",\"description\":\"Description\"," +
                "\"available\":true,\"owner\":1,\"requestId\":1}";

        ItemDto result = json.parseObject(content);

        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getName()).isEqualTo("Item");
        assertThat(result.getDescription()).isEqualTo("Description");
        assertThat(result.getAvailable()).isTrue();
        assertThat(result.getOwner()).isEqualTo(1);
        assertThat(result.getRequestId()).isEqualTo(1);
    }

    @Test
    void testDeserializeWithNullValues() throws Exception {
        String content = "{\"name\":\"Item\",\"available\":true}";

        ItemDto result = json.parseObject(content);

        assertThat(result.getName()).isEqualTo("Item");
        assertThat(result.getAvailable()).isTrue();
        assertThat(result.getId()).isNull();
        assertThat(result.getDescription()).isNull();
        assertThat(result.getOwner()).isNull();
    }
}
