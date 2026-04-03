package ru.practicum.shareit.item.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByItemId(Long itemId);

    @Query("SELECT c FROM Comment c " +
            "JOIN FETCH c.item i " +
            "JOIN FETCH c.author a " +
            "WHERE i.id IN ?1")
    List<Comment> findAllByItemIdIn(List<Long> itemIds);
}
