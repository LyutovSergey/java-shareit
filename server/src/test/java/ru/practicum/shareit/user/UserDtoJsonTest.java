package ru.practicum.shareit.user;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.user.dto.UserDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserDtoJsonTest {

    @Autowired
    private JacksonTester<UserDto> json;

    @Test
    void testUserDto() throws Exception {
        UserDto userDto = UserDto.builder()
                .id(1L)
                .name("Ivan")
                .email("ivan@mail.ru")
                .build();

        // Превращаем Java-объект в JSON
        JsonContent<UserDto> result = json.write(userDto);

        // Проверяем поля в JSON
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Ivan");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("ivan@mail.ru");
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\"id\":1, \"name\":\"Ivan\", \"email\":\"ivan@mail.ru\"}";

        UserDto dto = json.parse(content).getObject();

        // Проверяем, что Jackson правильно заполнил поля объекта
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Ivan");
        assertThat(dto.getEmail()).isEqualTo("ivan@mail.ru");
    }
}