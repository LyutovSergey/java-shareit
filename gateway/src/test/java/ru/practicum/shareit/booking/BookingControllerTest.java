package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private BookingClient bookingClient;

    private final String userIdHeader = "X-Sharer-User-Id";

    @Test
    void create_shouldReturnOkWhenValid() throws Exception {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(1L);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        when(bookingClient.create(eq(1L), any(BookingDto.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        mvc.perform(post("/bookings")
                        .header(userIdHeader, 1L)
                        .content(mapper.writeValueAsString(bookingDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(bookingClient).create(eq(1L), any(BookingDto.class));
    }

    @Test
    void create_shouldReturnBadRequestWhenStartIsInPast() throws Exception {
        BookingDto invalidDto = new BookingDto();
        invalidDto.setItemId(1L);
        invalidDto.setStart(LocalDateTime.now().minusDays(1)); // Прошлое
        invalidDto.setEnd(LocalDateTime.now().plusDays(1));

        mvc.perform(post("/bookings")
                        .header(userIdHeader, 1L)
                        .content(mapper.writeValueAsString(invalidDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }

    @Test
    void create_shouldReturnBadRequestWhenItemIdIsNull() throws Exception {
        BookingDto invalidDto = new BookingDto();
        invalidDto.setItemId(null); // Ошибка @NotNull
        invalidDto.setStart(LocalDateTime.now().plusDays(1));
        invalidDto.setEnd(LocalDateTime.now().plusDays(2));

        mvc.perform(post("/bookings")
                        .header(userIdHeader, 1L)
                        .content(mapper.writeValueAsString(invalidDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }

    @Test
    void approve_shouldReturnOk() throws Exception {
        when(bookingClient.approve(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        mvc.perform(patch("/bookings/1")
                        .header(userIdHeader, 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk());

        verify(bookingClient).approve(1L, 1L, true);
    }

    @Test
    void getById_shouldReturnOk() throws Exception {
        when(bookingClient.getById(1L, 10L))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        mvc.perform(get("/bookings/10")
                        .header(userIdHeader, 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getAllByBooker_shouldReturnOkWithState() throws Exception {
        when(bookingClient.getAllByBooker(eq(1L), eq("WAITING")))
                .thenReturn(new ResponseEntity<>(List.of(), HttpStatus.OK));

        mvc.perform(get("/bookings")
                        .header(userIdHeader, 1L)
                        .param("state", "WAITING"))
                .andExpect(status().isOk());

        verify(bookingClient).getAllByBooker(1L, "WAITING");
    }

    @Test
    void getAllByOwner_shouldReturnOkDefaultState() throws Exception {
        when(bookingClient.getAllByOwner(eq(1L), eq("ALL")))
                .thenReturn(new ResponseEntity<>(List.of(), HttpStatus.OK));

        mvc.perform(get("/bookings/owner")
                        .header(userIdHeader, 1L))
                .andExpect(status().isOk());

        verify(bookingClient).getAllByOwner(1L, "ALL");
    }
}
