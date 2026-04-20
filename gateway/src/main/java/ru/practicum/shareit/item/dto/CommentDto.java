package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentDto {

    private Long id;

    @NotBlank(message = "text не может быть пустым")
    private String text;

    private String authorName;
    private LocalDateTime created;
}