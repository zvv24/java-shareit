package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.user.dto.UserDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserDtoJsonTest {

    @Autowired
    private JacksonTester<UserDto> json;

    @Test
    void testSerialize() throws Exception {
        UserDto userDto = new UserDto(1, "User", "User@email.com");

        JsonContent<UserDto> result = json.write(userDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("User");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("User@email.com");
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\"id\":1,\"name\":\"User\",\"email\":\"User@email.com\"}";

        UserDto result = json.parseObject(content);

        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getName()).isEqualTo("User");
        assertThat(result.getEmail()).isEqualTo("User@email.com");
    }

    @Test
    void testDeserializeWithNullValues() throws Exception {
        String content = "{\"name\":\"User\"}";

        UserDto result = json.parseObject(content);

        assertThat(result.getName()).isEqualTo("User");
        assertThat(result.getId()).isNull();
        assertThat(result.getEmail()).isNull();
    }
}