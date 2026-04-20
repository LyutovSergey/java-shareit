package ru.practicum.shareit.request.dao;


import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    /**
     * Поиск всех запросов конкретного пользователя.
     * Используем ключевые слова OrderBy и Desc для автоматической сортировки по дате.
     */
    List<ItemRequest> findAllByRequestorIdOrderByCreatedDesc(Long requestorId);

    /**
     * Поиск запросов всех пользователей, кроме указанного (для эндпоинта /all).
     * Передаём объект Sort, чтобы гибко управлять сортировкой из сервиса.
     */
    List<ItemRequest> findAllByRequestorIdNot(Long userId, Sort sort);
}
