package ru.practicum.shareit.booking;


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

    @Test
    void testBookingDto() throws Exception {
        LocalDateTime start = LocalDateTime.of(2026, 4, 9, 13, 0, 0);
        LocalDateTime end = start.plusHours(1);

        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(1L);
        bookingDto.setStart(start);
        bookingDto.setEnd(end);

        JsonContent<BookingDto> result = json.write(bookingDto);

        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2026-04-09T13:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2026-04-09T14:00:00");
        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
    }
}
