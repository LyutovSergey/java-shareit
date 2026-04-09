package ru.practicum.shareit.request;


import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.UserService;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;


import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestServiceImplIntegrationTest {

    private final ItemRequestService requestService;
    private final UserService userService;

    @Test
    void create_shouldSaveRequestAndReturnDto() {
        UserDto user = userService.create(makeUserDto("Requester", "req@mail.ru"));
        ItemRequestDto dto = ItemRequestDto.builder().description("Нужен мощный перфоратор").build();

        ItemRequestDto saved = requestService.create(user.getId(), dto);

        assertThat(saved.getId(), notNullValue());
        assertThat(saved.getDescription(), equalTo("Нужен мощный перфоратор"));
        assertThat(saved.getCreated(), notNullValue());
    }

    @Test
    void create_shouldThrowNotFoundWhenUserDoesNotExist() {
        ItemRequestDto dto = ItemRequestDto.builder().description("Desc").build();
        assertThrows(NotFoundException.class, () -> requestService.create(999L, dto));
    }

    @Test
    void getUserRequests_shouldReturnRequestsInCorrectOrder() throws InterruptedException {
        UserDto user = userService.create(makeUserDto("User", "u@mail.ru"));
        requestService.create(user.getId(), ItemRequestDto.builder().description("First").build());
        Thread.sleep(10); // из-за скорости выполнения нарушается очередность
        requestService.create(user.getId(), ItemRequestDto.builder().description("Second").build());

        List<ItemRequestDto> result = requestService.getUserRequests(user.getId());

        assertThat(result, hasSize(2));
        assertThat(result.get(0).getDescription(), equalTo("Second")); // Последний созданный — первый в списке
        assertThat(result.get(1).getDescription(), equalTo("First"));
    }

    @Test
    void getAllRequests_shouldReturnOtherUsersRequestsOnly() {
        UserDto user1 = userService.create(makeUserDto("User1", "u1@mail.ru"));
        UserDto user2 = userService.create(makeUserDto("User2", "u2@mail.ru"));

        requestService.create(user1.getId(), ItemRequestDto.builder().description("From User 1").build());
        requestService.create(user2.getId(), ItemRequestDto.builder().description("From User 2").build());

        // Запрашивает user1, должен видеть только запрос от user2
        List<ItemRequestDto> result = requestService.getAllRequests(user1.getId());

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getDescription(), equalTo("From User 2"));
    }

    @Test
    void getById_shouldReturnRequestCorrectly() {
        UserDto user = userService.create(makeUserDto("Owner", "owner@mail.ru"));
        ItemRequestDto saved = requestService.create(user.getId(), ItemRequestDto.builder().description("Search").build());

        ItemRequestDto found = requestService.getById(user.getId(), saved.getId());

        assertThat(found.getId(), equalTo(saved.getId()));
        assertThat(found.getDescription(), equalTo("Search"));
    }

    @Test
    void getById_shouldThrowNotFoundWhenRequestIdIsInvalid() {
        UserDto user = userService.create(makeUserDto("User", "test@mail.ru"));

        Long invalidRequestId = 999L;
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> requestService.getById(user.getId(), invalidRequestId));

        assertThat(ex.getMessage(), containsString("Запрос с id " + invalidRequestId + " не найден"));
    }

    private UserDto makeUserDto(String name, String email) {
        return UserDto.builder().name(name).email(email).build();
    }
}

