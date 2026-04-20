package ru.practicum.shareit.booking.dto;

import java.util.Optional;

// Перечисление для поиска (State)
public enum BookingStateForFilter {
    ALL, CURRENT, PAST, FUTURE, WAITING, REJECTED;

    public static Optional<BookingStateForFilter> from(String stateStr) {
        for (BookingStateForFilter state : values()) {
            if (state.name().equalsIgnoreCase(stateStr)) {
                return Optional.of(state);
            }
        }
        return Optional.empty();
    }
}
