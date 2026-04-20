package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingResponseDtoJsonTest {

    @Autowired
    private JacksonTester<BookingResponseDto> json;

    @Test
    void testBookingResponseDto() throws Exception {
        LocalDateTime start = LocalDateTime.of(2026, 4, 9, 13, 0, 0);
        LocalDateTime end = start.plusHours(1);

        BookingResponseDto dto = BookingResponseDto.builder()
                .id(1L)
                .start(start)
                .end(end)
                .status(BookingStatus.APPROVED)
                .booker(UserDto.builder().id(2L).name("Booker").build())
                .item(ItemDto.builder().id(10L).name("ItemName").build())
                .build();

        JsonContent<BookingResponseDto> result = json.write(dto);

        // Проверка дат в формате ISO
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2026-04-09T13:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2026-04-09T14:00:00");

        // Проверка статуса
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("APPROVED");

        // Проверка вложенных объектов
        assertThat(result).extractingJsonPathNumberValue("$.booker.id").isEqualTo(2);
        assertThat(result).extractingJsonPathStringValue("$.item.name").isEqualTo("ItemName");
    }
}