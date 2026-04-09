package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dao.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;


import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemServiceImplIntegrationTest {

    private final ItemService itemService;
    private final UserService userService;
    private final ItemRequestRepository requestRepository;
    private final BookingRepository bookingRepository;

    @Test
    void create_shouldSaveItemAndLinkToRequest() {
        User owner = userService.findByIdOrException(userService.create(makeUserDto("Owner", "o@m.ru")).getId());
        User requester = userService.findByIdOrException(userService.create(makeUserDto("Req", "r@m.ru")).getId());

        ItemRequest request = requestRepository.save(ItemRequest.builder()
                .description("Need drill").requestor(requester).created(LocalDateTime.now()).build());

        ItemDto itemDto = ItemDto.builder()
                .name("Drill").description("Powerful").available(true).requestId(request.getId()).build();

        ItemDto saved = itemService.create(owner.getId(), itemDto);

        assertThat(saved.getId(), notNullValue());
        assertThat(saved.getRequestId(), equalTo(request.getId()));
    }

    @Test
    void update_shouldUpdateFields() {
        UserDto owner = userService.create(makeUserDto("Owner", "owner@mail.ru"));
        ItemDto saved = itemService.create(owner.getId(),
                ItemDto.builder().name("Old").description("Old").available(true).build());

        ItemDto update = ItemDto.builder().name("New").available(false).build();
        ItemDto updated = itemService.update(owner.getId(), saved.getId(), update);

        assertThat(updated.getName(), equalTo("New"));
        assertThat(updated.getAvailable(), is(false));
        assertThat(updated.getDescription(), equalTo("Old"));
    }

    @Test
    void update_shouldThrowForbiddenWhenNotOwner() {
        UserDto owner = userService.create(makeUserDto("Owner", "o@m.ru"));
        UserDto other = userService.create(makeUserDto("Other", "other@m.ru"));
        ItemDto saved = itemService.create(owner.getId(),
                ItemDto.builder().name("I").description("D").available(true).build());

        assertThrows(ForbiddenException.class, () ->
                itemService.update(other.getId(), saved.getId(), ItemDto.builder().name("X").build()));
    }

    @Test
    void getById_shouldReturnItemWithBookingsForOwner() {
        User owner = userService.findByIdOrException(userService.create(makeUserDto("O", "o@m.ru")).getId());
        User booker = userService.findByIdOrException(userService.create(makeUserDto("B", "b@m.ru")).getId());
        ItemDto itemDto = itemService.create(owner.getId(),
                ItemDto.builder().name("Item").description("D").available(true).build());

        // Создаем бронирование в прошлом
        bookingRepository.save(Booking.builder()
                .item(Item.builder().id(itemDto.getId()).owner(owner).build())
                .booker(booker).status(BookingStatus.APPROVED)
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1)).build());

        ItemDto result = itemService.getById(itemDto.getId(), owner.getId());

        assertThat(result.getLastBooking(), notNullValue());
        assertThat(result.getLastBooking().getBookerId(), equalTo(booker.getId()));
    }

    @Test
    void getAllByOwner_shouldReturnFullInfo() {
        UserDto owner = userService.create(makeUserDto("Owner", "owner@m.ru"));
        itemService.create(owner.getId(), ItemDto.builder().name("I1").description("D1").available(true).build());

        List<ItemDto> result = itemService.getAllByOwner(owner.getId());

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getName(), equalTo("I1"));
    }

    @Test
    void search_shouldFindOnlyAvailable() {
        UserDto owner = userService.create(makeUserDto("O", "o@m.ru"));
        itemService.create(owner.getId(), ItemDto.builder().name("Nail").description("Metal").available(true).build());
        itemService.create(owner.getId(), ItemDto.builder().name("Hammer").description("Metal").available(false).build());

        List<ItemDto> result = itemService.search("Metal");

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getName(), equalTo("Nail"));
    }

    @Test
    void createComment_shouldThrowExceptionWhenNoBooking() {
        UserDto owner = userService.create(makeUserDto("O", "o@m.ru"));
        UserDto booker = userService.create(makeUserDto("B", "b@m.ru"));
        ItemDto item = itemService.create(owner.getId(),
                ItemDto.builder().name("I").description("D").available(true).build());

        CommentDto comment = CommentDto.builder().text("Bad").build();

        assertThrows(BadRequestException.class, () ->
                itemService.createComment(booker.getId(), item.getId(), comment));
    }

    private UserDto makeUserDto(String name, String email) {
        return UserDto.builder().name(name).email(email).build();
    }

    @Test
    void getById_shouldThrowNotFoundWhenItemDoesNotExist() {
        UserDto user = userService.create(makeUserDto("User", "user@mail.com"));
        assertThrows(NotFoundException.class, () -> itemService.getById(999L, user.getId()));
    }
}
