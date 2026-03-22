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
    private String name;
    private String email;
}
