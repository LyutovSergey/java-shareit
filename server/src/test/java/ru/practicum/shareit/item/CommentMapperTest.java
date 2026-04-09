package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CommentMapperTest {

    @Test
    void toDto_ShouldMapCorrectly() {
        User author = User.builder().id(1L).name("Ivan").build();
        Comment comment = Comment.builder()
                .id(1L)
                .text("Great item!")
                .author(author)
                .created(LocalDateTime.now())
                .build();

        CommentDto dto = CommentMapper.toDto(comment);

        assertNotNull(dto);
        assertEquals(comment.getId(), dto.getId());
        assertEquals(comment.getText(), dto.getText());
        assertEquals(author.getName(), dto.getAuthorName());
        assertEquals(comment.getCreated(), dto.getCreated());
    }

    @Test
    void toComment_ShouldMapCorrectly() {
        CommentDto dto = CommentDto.builder()
                .text("New comment")
                .build();
        Item item = Item.builder().id(10L).name("Drill").build();
        User author = User.builder().id(1L).name("Ivan").build();

        Comment comment = CommentMapper.toComment(dto, item, author);

        assertNotNull(comment);
        assertEquals(dto.getText(), comment.getText());
        assertEquals(item, comment.getItem());
        assertEquals(author, comment.getAuthor());
        assertNotNull(comment.getCreated()); // Проверяем, что LocalDateTime.now() сработал
    }
}
