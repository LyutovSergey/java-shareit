package ru.practicum.shareit.user.dao;


import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Repository
public class InMemoryUserRepositoryImpl implements UserRepository {
    private final Map<Long, User> users = new HashMap<>();
    private Long idCounter = 1L;

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            user.setId(idCounter++);
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void deleteById(Long id) {
        users.remove(id);
    }

    @Override
    public boolean existsByEmail(String email, Long userId) {
        return users.values().stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(email)
                        && !Objects.equals(u.getId(), userId));
    }
}