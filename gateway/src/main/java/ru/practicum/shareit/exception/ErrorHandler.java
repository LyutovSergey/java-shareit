package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidation(final MethodArgumentNotValidException e) {
        log.error("Ошибка валидации: {}", e.getMessage());
        return Map.of("error", "Ошибка валидации данных");
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleEverything(final Throwable e) {
        log.error("Непредвиденная ошибка: {}", e.getMessage(), e);
        return Map.of("error", "Произошла внутренняя ошибка сервера");
    }

    @ExceptionHandler(BadRequestException.class) // Ловим именно наш класс
    @ResponseStatus(HttpStatus.BAD_REQUEST)      // Возвращаем статус 400
    public Map<String, String> handleBadRequest(final BadRequestException e) {
        log.error("Ошибка 400: {}", e.getMessage());
        return Map.of("error", e.getMessage());
    }

    // Добавляем этот метод для обработки отсутствующего заголовка X-Sharer-User-Id
    @ExceptionHandler(MissingRequestHeaderException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleMissingHeader(final MissingRequestHeaderException e) {
        log.error("Отсутствует обязательный заголовок: {}", e.getHeaderName());
        return Map.of("error", "Required request header '" + e.getHeaderName() + "' is not present");
    }
}
