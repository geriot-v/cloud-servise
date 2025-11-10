package ru.netology.cloudservise.security;

import ru.netology.cloudservise.entity.AuthToken;
import ru.netology.cloudservise.entity.User;
import ru.netology.cloudservise.repository.AuthTokenRepository;
import ru.netology.cloudservise.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class SecureTokenService implements UserDetailsService {

    private final AuthTokenRepository authTokenRepository;
    private final UserRepository userRepository; // Инжектим репозиторий напрямую
    private final PasswordEncoder passwordEncoder;

    @Value("${app.token.expiration-hours}")
    private int tokenExpirationHours;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + username));
    }

    @Transactional
    public String createToken(User user) {
        String rawToken = UUID.randomUUID().toString();
        String tokenHash = passwordEncoder.encode(rawToken);

        AuthToken token = new AuthToken();
        token.setTokenHash(tokenHash);
        token.setUser(user);
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusHours(tokenExpirationHours));

        authTokenRepository.save(token);
        log.debug("Создан новый токен для пользователя: {}", user.getLogin());

        return rawToken;
    }

    @Transactional(readOnly = true)
    public Optional<User> validateToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return Optional.empty();
        }

        try {
            LocalDateTime now = LocalDateTime.now();
            var activeTokens = authTokenRepository.findAllActiveTokens(now);

            for (AuthToken authToken : activeTokens) {
                if (passwordEncoder.matches(rawToken, authToken.getTokenHash())) {
                    User user = authToken.getUser();
                    log.debug("Токен подтвержден для пользователя: {}", user.getLogin());
                    return Optional.of(user);
                }
            }

            log.debug("Проверка токена не удалась: неверный или просроченный токен");
            return Optional.empty();

        } catch (Exception e) {
            log.error("Ошибка при проверке токена", e);
            return Optional.empty();
        }
    }

    @Transactional
    public void logout(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }

        try {
            LocalDateTime now = LocalDateTime.now();
            var activeTokens = authTokenRepository.findAllActiveTokens(now);

            for (AuthToken authToken : activeTokens) {
                if (passwordEncoder.matches(rawToken, authToken.getTokenHash())) {
                    authTokenRepository.delete(authToken);
                    log.debug("Пользователь вышел из системы");
                    return;
                }
            }

            log.debug("Токен для выхода не найден");

        } catch (Exception e) {
            log.error("Ошибка при выходе из системы", e);
        }
    }

    @Transactional
    public void cleanupExpiredTokens() {
        try {
            int deletedCount = authTokenRepository.deleteExpiredTokens(LocalDateTime.now());
            log.info("Очищено просроченных токенов: {}", deletedCount);
        } catch (Exception e) {
            log.error("Ошибка при очистке просроченных токенов", e);
        }
    }

    // Добавляем метод для проверки пользователя по логину и паролю
    @Transactional(readOnly = true)
    public Optional<User> authenticate(String login, String password) {
        Optional<User> userOpt = userRepository.findByLogin(login);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }

        User user = userOpt.get();
        if (passwordEncoder.matches(password, user.getPassword())) {
            return Optional.of(user);
        }

        return Optional.empty();
    }
}