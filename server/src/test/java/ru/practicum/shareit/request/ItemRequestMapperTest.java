package ru.practicum.shareit.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemRequestMapperTest {

    @Test
    @DisplayName("Маппинг в Entity")
    void toEntity_ShouldReturnEntityWithDescription() {
        ItemRequestDto dto = ItemRequestDto.builder()
                .description("Test description")
                .build();

        ItemRequest result = ItemRequestMapper.toEntity(dto);

        assertNotNull(result);
        assertEquals(dto.getDescription(), result.getDescription());
    }

    @Test
    @DisplayName("Маппинг в DTO: когда список вещей null (покрытие ветки else)")
    void toDto_WhenItemsIsNull_ShouldReturnEmptyList() {
        ItemRequest entity = ItemRequest.builder()
                .id(1L)
                .description("Desc")
                .created(LocalDateTime.now())
                .items(null) // Проверка ветки items != null -> false
                .build();

        ItemRequestDto dto = ItemRequestMapper.toDto(entity);

        assertNotNull(dto.getItems());
        assertTrue(dto.getItems().isEmpty());
    }

    @Test
    @DisplayName("Маппинг в DTO: полный маппинг со списком вещей и requestId")
    void toDto_WhenItemsPresent_ShouldMapEverything() {
        User owner = User.builder().id(10L).build();
        ItemRequest request = ItemRequest.builder().id(2L).build();

        Item item = Item.builder()
                .id(5L)
                .name("ItemName")
                .description("ItemDesc")
                .available(true)
                .owner(owner)
                .request(request) // Проверка ветки requestId != null -> true
                .build();

        ItemRequest entity = ItemRequest.builder()
                .id(2L)
                .description("RequestDesc")
                .created(LocalDateTime.now())
                .items(List.of(item)) // Проверка ветки items != null -> true
                .build();

        ItemRequestDto dto = ItemRequestMapper.toDto(entity);

        assertEquals(2L, dto.getId());
        assertEquals(1, dto.getItems().size());

        ItemRequestDto.ItemAnswerDto answer = dto.getItems().get(0);
        assertEquals(5L, answer.getId());
        assertEquals("ItemName", answer.getName());
        assertEquals(2L, answer.getRequestId());
        assertEquals(10L, answer.getOwnerId());
    }

    @Test
    @DisplayName("Маппинг в DTO: проверка случая requestId == null")
    void toDto_WhenItemHasNoRequest_ShouldReturnNullRequestId() {
        User owner = User.builder().id(10L).build();
        Item item = Item.builder()
                .id(5L)
                .owner(owner)
                .request(null) // Проверка ветки requestId != null -> false
                .build();

        ItemRequest entity = ItemRequest.builder()
                .items(List.of(item))
                .build();

        ItemRequestDto dto = ItemRequestMapper.toDto(entity);

        assertNull(dto.getItems().get(0).getRequestId());
    }
}
