package ru.practicum.shareit.booking;



import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private BookingResponseDto responseDto;
    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        responseDto = BookingResponseDto.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.WAITING)
                .booker(UserDto.builder().id(userId).name("User").build())
                .item(ItemDto.builder().id(10L).name("Item").build())
                .build();
    }

    @Test
    void create_Positive() throws Exception {
        BookingDto inputDto = BookingDto.builder()
                .itemId(10L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        when(bookingService.create(eq(userId), any(BookingDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDto.getId()))
                .andExpect(jsonPath("$.item.name").value("Item"))
                .andExpect(jsonPath("$.booker.id").value(userId));
    }

    @Test
    void approve_Positive() throws Exception {
        responseDto.setStatus(BookingStatus.APPROVED);
        when(bookingService.approve(eq(userId), eq(1L), anyBoolean())).thenReturn(responseDto);

        mockMvc.perform(patch("/bookings/{bookingId}", 1L)
                        .header("X-Sharer-User-Id", userId)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void getAllByBooker_Positive() throws Exception {
        when(bookingService.getAllByBooker(eq(userId), eq("FUTURE"))).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "FUTURE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // --- Негативные сценарии ---

    @Test
    void create_StartAfterEnd_Returns400() throws Exception {
        // Имитируем ошибку валидации дат из сервиса
        when(bookingService.create(anyLong(), any())).thenThrow(new BadRequestException("Неверные даты"));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new BookingDto())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void approve_ByNotOwner_Returns403() throws Exception {
        when(bookingService.approve(anyLong(), anyLong(), anyBoolean()))
                .thenThrow(new ForbiddenException("Только владелец может подтвердить"));

        mockMvc.perform(patch("/bookings/{bookingId}", 1L)
                        .header("X-Sharer-User-Id", 2L) // Другой юзер
                        .param("approved", "true"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllByOwner_UnknownState_Returns400() throws Exception {
        // Твой сервис кидает BadRequestException через getBookingStateForFilterOrException
        when(bookingService.getAllByOwner(anyLong(), eq("BLABLA")))
                .thenThrow(new BadRequestException("Статус BLABLA некорректный"));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "BLABLA"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getById_NoAccess_Returns404() throws Exception {
        when(bookingService.getById(anyLong(), anyLong()))
                .thenThrow(new NotFoundException("Нет прав на просмотр"));

        mockMvc.perform(get("/bookings/{bookingId}", 1L)
                        .header("X-Sharer-User-Id", 10L))
                .andExpect(status().isNotFound());
    }
}

