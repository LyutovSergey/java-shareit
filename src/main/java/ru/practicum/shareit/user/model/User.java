package ru.practicum.shareit.user.model;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

/**
 * TODO Sprint add-controllers.
 */

@Data
@Builder(toBuilder = true)
public class User {
    private Long id;

    @NotBlank(message = "name не может быть пустым")
    private String name;

    @Email(message = "Некорректный email")
    @NotBlank(message = "email не может быть пустым")
    private String email;
}
