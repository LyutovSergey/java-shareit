package ru.practicum.shareit.item;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemDtoJsonTest {

    @Autowired
    private JacksonTester<ItemDto> json;

    @Test
    void testItemDto() throws Exception {
        ItemDto dto = ItemDto.builder()
                .id(1L)
                .name("Отвертка")
                .available(true)
                .lastBooking(new ItemDto.BookingShortDto(10L, 2L))
                .comments(List.of(CommentDto.builder().id(1L).text("Круто").build()))
                .build();

        var result = json.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Отвертка");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isTrue();

        // Проверка вложенности
        assertThat(result).extractingJsonPathNumberValue("$.lastBooking.id").isEqualTo(10);
        assertThat(result).extractingJsonPathStringValue("$.comments[0].text").isEqualTo("Круто");
    }
}