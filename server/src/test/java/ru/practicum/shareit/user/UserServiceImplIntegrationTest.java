package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.UserService;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
@Transactional
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)

class UserServiceImplIntegrationTest {
    private final UserService userService;

    @Test
    void create_shouldSaveUserWithBuilder() {
        UserDto userDto = UserDto.builder()
                .name("Ivan")
                .email("ivan@mail.ru")
                .build();

        UserDto savedUser = userService.create(userDto);

        assertThat(savedUser.getId(), notNullValue());
        assertThat(savedUser.getName(), equalTo("Ivan"));
        assertThat(savedUser.getEmail(), equalTo("ivan@mail.ru"));
    }

    @Test
    void create_shouldSaveToDb() {
        UserDto userDto = UserDto.builder().name("Ivan2").email("ivan2@mail.ru").build();

        UserDto saved = userService.create(userDto);

        UserDto userDtoInDb = userService.getById(saved.getId());

        assertThat(userDtoInDb, notNullValue());
        assertThat(userDtoInDb.getName(), equalTo("Ivan2"));
        assertThat(userDtoInDb.getEmail(), equalTo("ivan2@mail.ru"));
    }

    @Test
    void create_shouldThrowConflictExceptionForDuplicateEmail() {
        UserDto user1 = UserDto.builder().name("U1").email("same@mail.ru").build();

        userService.create(user1);

        UserDto user2 = UserDto.builder().name("U2").email("same@mail.ru").build();

        assertThrows(ConflictException.class, () -> userService.create(user2));
    }

    @Test
    void update_shouldPartialUpdateWithToBuilder() {
        UserDto savedUser = userService.create(UserDto.builder()
                .name("Old Name")
                .email("old@mail.ru")
                .build());

        UserDto updateRequest = savedUser.toBuilder()
                .name("New Name")
                .build();

        UserDto updatedUser = userService.update(savedUser.getId(), updateRequest);

        assertThat(updatedUser.getName(), equalTo("New Name"));
        assertThat(updatedUser.getEmail(), equalTo("old@mail.ru"));
    }

    @Test
    void update_shouldPartialUpdateAndShouldSaveToDb() {
        UserDto savedUserDto = userService.create(UserDto.builder()
                .name("Old Name2")
                .email("old2@mail.ru")
                .build());

        UserDto updateRequest = UserDto.builder()
                .name("New Name2")
                .build();

        userService.update(savedUserDto.getId(), updateRequest);

        UserDto userInDb = userService.getById(savedUserDto.getId());

        assertThat("Имя в базе должно быть обновлено",
                userInDb.getName(), equalTo("New Name2"));
        assertThat("Email в базе не должен был измениться",
                userInDb.getEmail(), equalTo("old2@mail.ru"));
    }

    @Test
    void getById_shouldThrowNotFoundException() {
        assertThrows(NotFoundException.class, () -> userService.getById(999L));
    }

    @Test
    void getAll_shouldReturnAllUsers() {
        userService.create(UserDto.builder().name("User1").email("1@mail.ru").build());
        userService.create(UserDto.builder().name("User2").email("2@mail.ru").build());

        List<UserDto> result = userService.getAll();

        assertThat(result, hasSize(greaterThanOrEqualTo(2)));
        assertThat(result, hasItem(hasProperty("email", is("1@mail.ru"))));
        assertThat(result, hasItem(hasProperty("email", is("2@mail.ru"))));
    }

    @Test
    void delete_shouldRemoveUser() {
        UserDto user = userService.create(UserDto.builder().name("Del").email("del@mail.ru").build());

        userService.delete(user.getId());

        assertThrows(NotFoundException.class, () -> userService.getById(user.getId()));
    }
}