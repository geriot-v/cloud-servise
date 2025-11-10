package ru.netology.cloudservise.config;

import ru.netology.cloudservise.security.SecureTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class SchedulingConfig {

    private final SecureTokenService tokenService;

    @Scheduled(fixedRate = 6 * 60 * 60 * 1000) // 6 часов
    public void cleanupExpiredTokens() {
        log.debug("Запуск очистки просроченных токенов");
        tokenService.cleanupExpiredTokens();
    }
}
