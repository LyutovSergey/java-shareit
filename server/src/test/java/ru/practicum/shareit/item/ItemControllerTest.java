package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    private ItemDto itemDto;
    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        itemDto = ItemDto.builder()
                .id(1L)
                .name("Дрель")
                .description("Электрическая дрель")
                .available(true)
                .lastBooking(new ItemDto.BookingShortDto(10L, 2L))
                .nextBooking(new ItemDto.BookingShortDto(11L, 3L))
                .comments(List.of())
                .build();
    }

    @Test
    void create_Positive() throws Exception {
        when(itemService.create(eq(userId), any(ItemDto.class))).thenReturn(itemDto);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemDto.getId()))
                .andExpect(jsonPath("$.name").value(itemDto.getName()));
    }

    @Test
    void update_Positive() throws Exception {
        ItemDto updateDto = ItemDto.builder().name("Новая дрель").build();
        ItemDto updatedDto = itemDto.toBuilder().name("Новая дрель").build();

        when(itemService.update(eq(userId), eq(1L), any(ItemDto.class))).thenReturn(updatedDto);

        mockMvc.perform(patch("/items/{itemId}", 1L)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Новая дрель"));
    }

    @Test
    void getById_Positive_WithBookings() throws Exception {
        when(itemService.getById(1L, userId)).thenReturn(itemDto);

        mockMvc.perform(get("/items/{itemId}", 1L)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastBooking.id").value(10L))
                .andExpect(jsonPath("$.nextBooking.id").value(11L));
    }

    @Test
    void search_Positive() throws Exception {
        when(itemService.search("дрель")).thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items/search")
                        .param("text", "дрель"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Дрель"));
    }

    @Test
    void createComment_Positive() throws Exception {
        CommentDto commentDto = CommentDto.builder().text("Супер!").build();
        CommentDto savedComment = CommentDto.builder()
                .id(1L)
                .text("Супер!")
                .authorName("Ivan")
                .created(LocalDateTime.now())
                .build();

        when(itemService.createComment(eq(userId), eq(1L), any(CommentDto.class)))
                .thenReturn(savedComment);

        mockMvc.perform(post("/items/{itemId}/comment", 1L)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authorName").value("Ivan"))
                .andExpect(jsonPath("$.text").value("Супер!"));
    }

    // --- Негативные сценарии ---

    @Test
    void update_Forbidden_WhenNotOwner() throws Exception {
        when(itemService.update(anyLong(), anyLong(), any(ItemDto.class)))
                .thenThrow(new ForbiddenException("Редактировать может только владелец"));

        mockMvc.perform(patch("/items/{itemId}", 1L)
                        .header("X-Sharer-User-Id", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createComment_BadRequest_NoBooking() throws Exception {
        when(itemService.createComment(anyLong(), anyLong(), any(CommentDto.class)))
                .thenThrow(new BadRequestException("Вы не бронировали вещь"));

        mockMvc.perform(post("/items/{itemId}/comment", 1L)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CommentDto())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getById_NotFound_Returns404() throws Exception {
        when(itemService.getById(anyLong(), anyLong()))
                .thenThrow(new NotFoundException("Вещь не найдена"));

        mockMvc.perform(get("/items/{itemId}", 999L)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isNotFound());
    }
}
