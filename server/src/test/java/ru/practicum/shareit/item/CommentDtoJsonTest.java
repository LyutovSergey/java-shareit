package ru.practicum.shareit.item;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.item.dto.CommentDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class CommentDtoJsonTest {

    @Autowired
    private JacksonTester<CommentDto> json;

    @Test
    void testCommentDto() throws Exception {
        LocalDateTime created = LocalDateTime.of(2026, 4, 9, 10, 0, 0);
        CommentDto dto = CommentDto.builder()
                .id(1L)
                .text("Всё отлично")
                .authorName("Ivan")
                .created(created)
                .build();

        var result = json.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo("2026-04-09T10:00:00");
        assertThat(result).extractingJsonPathStringValue("$.authorName").isEqualTo("Ivan");
    }
}