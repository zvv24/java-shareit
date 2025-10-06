package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingDtoJsonTest {

    @Autowired
    private JacksonTester<BookingDto> json;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testSerialize() throws Exception {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(1);
        bookingDto.setItemId(1);
        LocalDateTime start = LocalDateTime.of(2025, 10, 5, 21, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 10, 6, 21, 0, 0);
        bookingDto.setStart(start);
        bookingDto.setEnd(end);

        JsonContent<BookingDto> result = json.write(bookingDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start")
                .isEqualTo("2025-10-05T21:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end")
                .isEqualTo("2025-10-06T21:00:00");
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\"id\":1,\"itemId\":1,\"start\":\"2025-10-05T21:00:00\",\"end\":\"2025-10-06T21:00:00\"}";
        BookingDto result = json.parseObject(content);

        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getItemId()).isEqualTo(1);
        assertThat(result.getStart()).isEqualTo(LocalDateTime.of(2025, 10, 5, 21, 0));
        assertThat(result.getEnd()).isEqualTo(LocalDateTime.of(2025, 10, 6, 21, 0));
    }

    @Test
    void testDeserializeWithNullValues() throws Exception {
        String content = "{\"itemId\":1}";

        BookingDto result = json.parseObject(content);

        assertThat(result.getItemId()).isEqualTo(1);
        assertThat(result.getId()).isNull();
        assertThat(result.getStart()).isNull();
        assertThat(result.getEnd()).isNull();
    }
}
