package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class UserDto {
    private Long id;

    @NotBlank(message = "name не может быть пустым")
    private String name;

    @Email(message = "Некорректный email")
    @NotBlank(message = "email не может быть пустым")
    private String email;
}
