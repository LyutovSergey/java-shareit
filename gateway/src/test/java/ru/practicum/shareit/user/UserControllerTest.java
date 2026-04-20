package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private UserClient userClient;

    @Test
    void create_shouldReturnOkWhenUserIsValid() throws Exception {
        UserDto validUser = UserDto.builder()
                .name("Ivan")
                .email("ivan@mail.ru")
                .build();

        UserDto savedUser = validUser.toBuilder().id(1L).build();

        // Имитируем успешный ответ от сервера через клиент
        when(userClient.create(any(UserDto.class)))
                .thenReturn(new ResponseEntity<>(savedUser, HttpStatus.OK));

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(validUser))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Ivan"))
                .andExpect(jsonPath("$.email").value("ivan@mail.ru"));

        verify(userClient, times(1)).create(any(UserDto.class));
    }

    @Test
    void create_shouldReturnBadRequestWhenEmailIsInvalid() throws Exception {
        UserDto invalidUser = UserDto.builder()
                .name("Valid Name")
                .email("not-an-email") // Некорректный email
                .build();

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(invalidUser))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        // Запрос НЕ должен дойти до клиента
        verifyNoInteractions(userClient);
    }

    @Test
    void create_shouldReturnBadRequestWhenNameIsBlank() throws Exception {
        UserDto invalidUser = UserDto.builder()
                .name(" ") // Пустое имя
                .email("valid@mail.ru")
                .build();

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(invalidUser))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userClient);
    }

    @Test
    void create_shouldReturnBadRequestWhenEmailIsBlank() throws Exception {
        UserDto invalidUser = UserDto.builder()
                .name("Ivan")
                .email("") // Пустой email
                .build();

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(invalidUser))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userClient);
    }

    @Test
    void update_shouldReturnOk() throws Exception {
        UserDto updateDto = UserDto.builder().name("Updated").build();
        when(userClient.update(eq(1L), any())).thenReturn(new ResponseEntity<>(updateDto, HttpStatus.OK));

        mvc.perform(patch("/users/1")
                        .content(mapper.writeValueAsString(updateDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));

        verify(userClient).update(eq(1L), any());
    }

    @Test
    void getById_shouldReturnUser() throws Exception {
        UserDto userDto = UserDto.builder().id(1L).name("Ivan").build();
        when(userClient.getById(1L)).thenReturn(new ResponseEntity<>(userDto, HttpStatus.OK));

        mvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Ivan"));
    }

    @Test
    void getAll_shouldReturnListWithUsers() throws Exception {

        UserDto user1 = UserDto.builder().id(1L).name("Ivan").email("ivan@mail.ru").build();
        UserDto user2 = UserDto.builder().id(2L).name("Petr").email("petr@mail.ru").build();
        List<UserDto> allUsers = List.of(user1, user2);

        when(userClient.getAll()).thenReturn(new ResponseEntity<>(allUsers, HttpStatus.OK));
        mvc.perform(get("/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // Проверяем формат ответа
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // Проверяем размер списка
                .andExpect(jsonPath("$.length()").value(2))
                // Проверяем содержимое первого элемента
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Ivan"))
                .andExpect(jsonPath("$[0].email").value("ivan@mail.ru"))
                // Проверяем содержимое второго элемента
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("Petr"));

        // Убеждаемся, что вызов клиента был совершен ровно 1 раз
        verify(userClient, times(1)).getAll();
    }

    @Test
    void delete_shouldCallClient() throws Exception {
        when(userClient.delete(1L)).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        mvc.perform(delete("/users/1"))
                .andExpect(status().isOk());

        verify(userClient).delete(1L);
    }
}