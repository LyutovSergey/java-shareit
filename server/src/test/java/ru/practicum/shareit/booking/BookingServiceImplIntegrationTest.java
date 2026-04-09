package ru.practicum.shareit.booking;


import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceImplIntegrationTest {

    private final BookingService bookingService;
    private final UserService userService;
    private final ItemService itemService;

    @Test
    void create_shouldSaveBookingWhenValid() {
        UserDto owner = userService.create(makeUser("Owner", "owner@mail.com"));
        UserDto booker = userService.create(makeUser("Booker", "booker@mail.com"));
        ItemDto item = itemService.create(owner.getId(), makeItem("Item", true));

        BookingDto dto = new BookingDto();
        dto.setItemId(item.getId());
        dto.setStart(LocalDateTime.now().plusDays(1));
        dto.setEnd(LocalDateTime.now().plusDays(2));

        BookingResponseDto saved = bookingService.create(booker.getId(), dto);

        assertThat(saved.getId(), notNullValue());
        assertThat(saved.getStatus(), equalTo(BookingStatus.WAITING));
        assertThat(saved.getBooker().getId(), equalTo(booker.getId()));
    }

    @Test
    void create_shouldThrowNotFoundWhenItemDoesNotExist() {
        UserDto booker = userService.create(makeUser("Booker", "b@m.ru"));
        BookingDto dto = new BookingDto();
        dto.setItemId(999L);
        dto.setStart(LocalDateTime.now().plusDays(1));
        dto.setEnd(LocalDateTime.now().plusDays(2));

        assertThrows(NotFoundException.class, () -> bookingService.create(booker.getId(), dto));
    }

    @Test
    void create_shouldThrowBadRequestWhenItemNotAvailable() {
        UserDto owner = userService.create(makeUser("Owner", "o@m.ru"));
        UserDto booker = userService.create(makeUser("Booker", "b@m.ru"));
        ItemDto item = itemService.create(owner.getId(), makeItem("Item", false));

        BookingDto dto = new BookingDto();
        dto.setItemId(item.getId());
        dto.setStart(LocalDateTime.now().plusDays(1));
        dto.setEnd(LocalDateTime.now().plusDays(2));

        assertThrows(BadRequestException.class, () -> bookingService.create(booker.getId(), dto));
    }

    @Test
    void create_shouldThrowBadRequestWhenBookingOwnItem() {
        UserDto owner = userService.create(makeUser("Owner", "o@m.ru"));
        ItemDto item = itemService.create(owner.getId(), makeItem("Item", true));

        BookingDto dto = new BookingDto();
        dto.setItemId(item.getId());
        dto.setStart(LocalDateTime.now().plusDays(1));
        dto.setEnd(LocalDateTime.now().plusDays(2));

        assertThrows(BadRequestException.class, () -> bookingService.create(owner.getId(), dto));
    }

    @Test
    void approve_shouldUpdateStatusCorrectly() {
        UserDto owner = userService.create(makeUser("Owner", "o@m.ru"));
        UserDto booker = userService.create(makeUser("Booker", "b@m.ru"));
        ItemDto item = itemService.create(owner.getId(), makeItem("Item", true));

        BookingDto dto = new BookingDto();
        dto.setItemId(item.getId());
        dto.setStart(LocalDateTime.now().plusDays(1));
        dto.setEnd(LocalDateTime.now().plusDays(2));
        BookingResponseDto booking = bookingService.create(booker.getId(), dto);

        BookingResponseDto approved = bookingService.approve(owner.getId(), booking.getId(), true);

        assertThat(approved.getStatus(), equalTo(BookingStatus.APPROVED));
    }

    @Test
    void approve_shouldThrowForbiddenWhenNotOwner() {
        UserDto owner = userService.create(makeUser("Owner", "o@m.ru"));
        UserDto booker = userService.create(makeUser("Booker", "b@m.ru"));
        ItemDto item = itemService.create(owner.getId(), makeItem("Item", true));

        BookingDto dto = new BookingDto();
        dto.setItemId(item.getId());
        dto.setStart(LocalDateTime.now().plusDays(1));
        dto.setEnd(LocalDateTime.now().plusDays(2));
        BookingResponseDto booking = bookingService.create(booker.getId(), dto);

        assertThrows(ForbiddenException.class, () ->
                bookingService.approve(booker.getId(), booking.getId(), true));
    }

    @Test
    void getById_shouldThrowNotFoundWhenBookingDoesNotExist() {
        UserDto user = userService.create(makeUser("User", "user@test.ru"));
        assertThrows(NotFoundException.class, () -> bookingService.getById(user.getId(), 999L));
    }

    @Test
    void getById_shouldThrowNotFoundWhenUserIsNotAuthorized() {
        UserDto owner = userService.create(makeUser("Owner", "o@m.ru"));
        UserDto booker = userService.create(makeUser("Booker", "b@m.ru"));
        UserDto stranger = userService.create(makeUser("Stranger", "s@m.ru"));
        ItemDto item = itemService.create(owner.getId(), makeItem("Item", true));

        BookingDto dto = new BookingDto();
        dto.setItemId(item.getId());
        dto.setStart(LocalDateTime.now().plusDays(1));
        dto.setEnd(LocalDateTime.now().plusDays(2));
        BookingResponseDto booking = bookingService.create(booker.getId(), dto);

        // Посторонний пользователь получает 404 (NotFound)
        assertThrows(NotFoundException.class, () -> bookingService.getById(stranger.getId(), booking.getId()));
    }

    @Test
    void getAllByBooker_shouldReturnBookings() {
        UserDto owner = userService.create(makeUser("Owner", "o@m.ru"));
        UserDto booker = userService.create(makeUser("Booker", "b@m.ru"));
        ItemDto item = itemService.create(owner.getId(), makeItem("Item", true));

        BookingDto dto = new BookingDto();
        dto.setItemId(item.getId());
        dto.setStart(LocalDateTime.now().plusDays(1));
        dto.setEnd(LocalDateTime.now().plusDays(2));
        bookingService.create(booker.getId(), dto);

        List<BookingResponseDto> result = bookingService.getAllByBooker(booker.getId(), "ALL");

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getBooker().getId(), equalTo(booker.getId()));
    }

    @Test
    void getAllByBooker_shouldThrowBadRequestOnUnknownState() {
        UserDto user = userService.create(makeUser("User", "u@m.ru"));
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                bookingService.getAllByBooker(user.getId(), "UNSUPPORTED_STATE"));

        assertThat(ex.getMessage(), containsString("Статус  UNSUPPORTED_STATE некорректный"));
    }

    private UserDto makeUser(String name, String email) {
        return UserDto.builder().name(name).email(email).build();
    }

    private ItemDto makeItem(String name, boolean available) {
        return ItemDto.builder().name(name).description("Description").available(available).build();
    }
}