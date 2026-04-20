package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemRequestClient itemRequestClient;

    private final String userIdHeader = "X-Sharer-User-Id";

    @Test
    void create_shouldReturnOkWhenRequestIsValid() throws Exception {
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("Нужна дрель")
                .build();

        ItemRequestDto responseDto = requestDto.toBuilder()
                .id(1L)
                .created(LocalDateTime.now())
                .build();

        when(itemRequestClient.create(eq(1L), any(ItemRequestDto.class)))
                .thenReturn(new ResponseEntity<>(responseDto, HttpStatus.OK));

        mvc.perform(post("/requests")
                        .header(userIdHeader, 1L)
                        .content(mapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Нужна дрель"));

        verify(itemRequestClient).create(eq(1L), any(ItemRequestDto.class));
    }

    @Test
    void create_shouldReturnBadRequestWhenDescriptionIsBlank() throws Exception {
        ItemRequestDto invalidDto = ItemRequestDto.builder().description("").build();

        mvc.perform(post("/requests")
                        .header(userIdHeader, 1L)
                        .content(mapper.writeValueAsString(invalidDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemRequestClient);
    }

    @Test
    void create_shouldReturnBadRequestWhenNoUserIdHeader() throws Exception {
        ItemRequestDto requestDto = ItemRequestDto.builder().description("Description").build();

        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()); // Spring выбросит ошибку из-за отсутствия обязательного хедера

        verifyNoInteractions(itemRequestClient);
    }

    @Test
    void getUserRequests_shouldReturnList() throws Exception {
        ItemRequestDto response = ItemRequestDto.builder()
                .id(1L)
                .description("Request")
                .items(List.of())
                .build();

        when(itemRequestClient.getUserRequests(1L))
                .thenReturn(new ResponseEntity<>(List.of(response), HttpStatus.OK));

        mvc.perform(get("/requests")
                        .header(userIdHeader, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(itemRequestClient).getUserRequests(1L);
    }

    @Test
    void getAll_shouldReturnList() throws Exception {
        when(itemRequestClient.getAll(1L))
                .thenReturn(new ResponseEntity<>(List.of(), HttpStatus.OK));

        mvc.perform(get("/requests/all")
                        .header(userIdHeader, 1L))
                .andExpect(status().isOk());

        verify(itemRequestClient).getAll(1L);
    }

    @Test
    void getById_shouldReturnRequest() throws Exception {
        ItemRequestDto response = ItemRequestDto.builder().id(10L).description("Text").build();

        when(itemRequestClient.getById(1L, 10L))
                .thenReturn(new ResponseEntity<>(response, HttpStatus.OK));

        mvc.perform(get("/requests/10")
                        .header(userIdHeader, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.description").value("Text"));

        verify(itemRequestClient).getById(1L, 10L);
    }
}
