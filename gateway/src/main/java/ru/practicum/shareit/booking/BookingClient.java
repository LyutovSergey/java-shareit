package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.client.BaseClient;
import java.util.Map;

/**
 * Клиент для отправки запросов на основной сервер ShareIt.
 * Использует методы BaseClient для выполнения HTTP-обмена.
 */
@Service
public class BookingClient extends BaseClient {
    private static final String API_PREFIX = "/bookings";

    @Autowired
    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> create(Long userId, BookingDto bookingDto) {
        // Вызывает protected <T> ResponseEntity<Object> post(String path, long userId, T body)
        return post("", userId, bookingDto);
    }

    public ResponseEntity<Object> approve(Long userId, Long bookingId, Boolean approved) {
        Map<String, Object> parameters = Map.of("approved", approved);
        // Вызывает метод patch с параметрами строки запроса
        return patch("/" + bookingId + "?approved={approved}", userId, parameters, null);
    }

    public ResponseEntity<Object> getById(Long userId, Long bookingId) {
        // Вызывает protected ResponseEntity<Object> get(String path, long userId)
        return get("/" + bookingId, userId);
    }

    public ResponseEntity<Object> getAllByBooker(Long userId, String state) {
        Map<String, Object> parameters = Map.of("state", state);
        // Вызывает GET запрос с фильтрацией по state
        return get("?state={state}", userId, parameters);
    }

    public ResponseEntity<Object> getAllByOwner(Long userId, String state) {
        Map<String, Object> parameters = Map.of("state", state);
        // Вызывает GET запрос для владельца вещей
        return get("/owner?state={state}", userId, parameters);
    }
}