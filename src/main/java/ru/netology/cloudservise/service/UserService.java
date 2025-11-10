package ru.netology.cloudservise.service;

import ru.netology.cloudservise.entity.User;
import ru.netology.cloudservise.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

/**
 * Сервис для управления пользователями.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Убираем метод findByLoginAndPassword, так как он теперь в SecureTokenService
    // Оставляем только методы для управления пользователями

    @Transactional(readOnly = true)
    public Optional<User> findByLogin(String login) {
        return userRepository.findByLogin(login);
    }

    @Transactional(readOnly = true)
    public boolean userExists(String login) {
        return userRepository.existsByLogin(login);
    }

    @Transactional
    public User createUser(String login, String password) {
        log.info("Создание нового пользователя: {}", login);

        if (userRepository.existsByLogin(login)) {
            throw new IllegalArgumentException("Пользователь с логином " + login + " уже существует");
        }

        User user = new User();
        user.setLogin(login);

        String hashedPassword = passwordEncoder.encode(password);
        user.setPassword(hashedPassword);

        User savedUser = userRepository.save(user);
        log.info("Пользователь успешно создан: {}", login);

        return savedUser;
    }

    @Transactional
    public User createUserWithHashedPassword(String login, String hashedPassword) {
        log.info("Создание пользователя с предварительно захэшированным паролем: {}", login);

        if (userRepository.existsByLogin(login)) {
            throw new IllegalArgumentException("Пользователь с логином " + login + " уже существует");
        }

        if (!hashedPassword.startsWith("$2a")) {
            throw new IllegalArgumentException("Пароль должен быть предварительно захэширован с использованием BCrypt");
        }

        User user = new User();
        user.setLogin(login);
        user.setPassword(hashedPassword);

        return userRepository.save(user);
    }
}