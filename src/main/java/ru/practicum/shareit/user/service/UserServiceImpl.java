package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto create(UserDto userDto) {
        log.info("Создание пользователя с email: {}", userDto.getEmail());
        validateEmailUnique(userDto.getEmail(), null);
        User savedUser = userRepository.save(UserMapper.toUser(userDto));
        log.debug("{}", savedUser); // Вывод созданного объекта
        return UserMapper.toUserDto(savedUser);
    }

    @Override
    public UserDto update(Long userId, UserDto userDto) {
        log.info("Обновление пользователя с id: {}", userId);
        User userInRepository = findByIdOrException(userId);

        // Обновляем email, если он передан и отличается от текущего
        if (StringUtils.hasText(userDto.getEmail()) && !userDto.getEmail().equals(userInRepository.getEmail())) {
            validateEmailFormat(userDto.getEmail());
            validateEmailUnique(userDto.getEmail(), userId);
            userInRepository.setEmail(userDto.getEmail());
        }

        // Обновляем имя, если оно передано
        if (StringUtils.hasText(userDto.getName())) {
            userInRepository.setName(userDto.getName());
        }
        User updatedUser = userRepository.save(userInRepository);
        log.debug("{}", updatedUser); // Вывод обновленного объекта
        return UserMapper.toUserDto(updatedUser);
    }

    @Override
    public UserDto getById(Long userId) {
        log.info("Получение пользователя по id: {}", userId);

        User user = findByIdOrException(userId);

        log.debug("{}", user); // Вывод найденного объекта
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        log.info("Получение списка всех пользователей");

        List<User> users = userRepository.findAll();
        log.debug("{}", users); // Вывод списка всех объектов

        return users.stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    public void delete(Long userId) {
        log.info("Удаление пользователя с id: {}", userId);

        // Проверяем существование перед удалением
        findByIdOrException(userId);

        userRepository.deleteById(userId);
        log.debug("Пользователь с id {} успешно удален", userId);
    }

    private void validateEmailUnique(String email, Long userId) {
        // Проверка уникальности email через репозиторий
        if (userRepository.existsByEmail(email, userId)) {
            log.warn("Конфликт: email {} уже занят", email);
            throw new ConflictException("Email " + email + " уже занят");
        }
    }

    public User findByIdOrException(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь с id {} не найден", userId);
                    return new NotFoundException("Пользователь c id " + userId + " не найден");
                });

    }

    private void validateEmailFormat(String email) {
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new BadRequestException("Некорректный формат email " + email);
        }
    }
}
