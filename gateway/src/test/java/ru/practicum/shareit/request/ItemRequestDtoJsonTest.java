package ru.practicum.shareit.request;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    void testItemRequestDto() throws Exception {
        LocalDateTime created = LocalDateTime.of(2026, 4, 9, 10, 0, 0);
        ItemRequestDto dto = ItemRequestDto.builder()
                .id(1L)
                .description("Ищу перфоратор")
                .created(created)
                .items(List.of(ItemRequestDto.ItemAnswerDto.builder().id(5L).name("Bosch").build()))
                .build();

        var result = json.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo("2026-04-09T10:00:00");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Ищу перфоратор");
        assertThat(result).extractingJsonPathStringValue("$.items[0].name").isEqualTo("Bosch");
    }
}