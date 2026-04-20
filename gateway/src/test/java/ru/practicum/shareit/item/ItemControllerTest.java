package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemClient itemClient;
    private final String userIdHeader = "X-Sharer-User-Id";

    @Test
    void create_shouldReturnOkWhenItemIsValid() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .name("Дрель")
                .description("Мощная дрель")
                .available(true)
                .build();

        when(itemClient.create(eq(1L), any(ItemDto.class)))
                .thenReturn(new ResponseEntity<>(itemDto, HttpStatus.OK));

        mvc.perform(post("/items")
                        .header(userIdHeader, 1L)
                        .content(mapper.writeValueAsString(itemDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Дрель"))
                .andExpect(jsonPath("$.available").value(true));

        verify(itemClient).create(eq(1L), any(ItemDto.class));
    }

    @Test
    void create_shouldReturnBadRequestWhenAvailableIsNull() throws Exception {
        ItemDto invalidDto = ItemDto.builder()
                .name("Дрель")
                .description("Описание")
                .available(null) // Нарушение @NotNull
                .build();

        mvc.perform(post("/items")
                        .header(userIdHeader, 1L)
                        .content(mapper.writeValueAsString(invalidDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemClient);
    }

    @Test
    void update_shouldReturnOk() throws Exception {
        ItemDto updateDto = ItemDto.builder().name("Новое имя").build();

        when(itemClient.update(eq(1L), eq(10L), any(ItemDto.class)))
                .thenReturn(new ResponseEntity<>(updateDto, HttpStatus.OK));

        mvc.perform(patch("/items/10")
                        .header(userIdHeader, 1L)
                        .content(mapper.writeValueAsString(updateDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Новое имя"));
    }

    @Test
    void getById_shouldReturnItem() throws Exception {
        ItemDto itemDto = ItemDto.builder().id(10L).name("Вещь").build();

        when(itemClient.getById(1L, 10L))
                .thenReturn(new ResponseEntity<>(itemDto, HttpStatus.OK));

        mvc.perform(get("/items/10")
                        .header(userIdHeader, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L));
    }

    @Test
    void getAll_shouldReturnList() throws Exception {
        when(itemClient.getAll(1L))
                .thenReturn(new ResponseEntity<>(List.of(), HttpStatus.OK));

        mvc.perform(get("/items")
                        .header(userIdHeader, 1L))
                .andExpect(status().isOk());
    }

    @Test
    void search_shouldReturnList() throws Exception {
        when(itemClient.search(1L, "дрель"))
                .thenReturn(new ResponseEntity<>(List.of(), HttpStatus.OK));

        mvc.perform(get("/items/search")
                        .header(userIdHeader, 1L)
                        .param("text", "дрель"))
                .andExpect(status().isOk());
    }

    @Test
    void createComment_shouldReturnOkWhenValid() throws Exception {
        CommentDto commentDto = CommentDto.builder().text("Отличная вещь!").build();

        when(itemClient.createComment(eq(1L), eq(10L), any(CommentDto.class)))
                .thenReturn(new ResponseEntity<>(commentDto, HttpStatus.OK));

        mvc.perform(post("/items/10/comment")
                        .header(userIdHeader, 1L)
                        .content(mapper.writeValueAsString(commentDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Отличная вещь!"));
    }

    @Test
    void createComment_shouldReturnBadRequestWhenTextIsBlank() throws Exception {
        CommentDto invalidComment = CommentDto.builder().text("").build();

        mvc.perform(post("/items/10/comment")
                        .header(userIdHeader, 1L)
                        .content(mapper.writeValueAsString(invalidComment))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemClient);
    }

    @Test
    void search_shouldReturnEmptyListWhenTextIsBlank() throws Exception {
        mvc.perform(get("/items/search")
                        .header(userIdHeader, 1L)
                        .param("text", "  "))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verifyNoInteractions(itemClient);
    }
}
