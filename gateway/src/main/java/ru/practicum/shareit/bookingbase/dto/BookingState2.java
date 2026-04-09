package ru.practicum.shareit.bookingbase.dto;

import java.util.Optional;

public enum BookingState2 {
	// Все
	ALL,
	// Текущие
	CURRENT,
	// Будущие
	FUTURE,
	// Завершенные
	PAST,
	// Отклоненные
	REJECTED,
	// Ожидающие подтверждения
	WAITING;

	public static Optional<BookingState2> from(String stringState) {
		for (BookingState2 state : values()) {
			if (state.name().equalsIgnoreCase(stringState)) {
				return Optional.of(state);
			}
		}
		return Optional.empty();
	}
}
