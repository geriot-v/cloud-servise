package ru.netology.cloudservise.controller;

import ru.netology.cloudservise.dto.ErrorResponse;
import ru.netology.cloudservise.dto.LoginRequest;
import ru.netology.cloudservise.dto.LoginResponse;
import ru.netology.cloudservise.entity.User;
import ru.netology.cloudservise.security.SecureTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final SecureTokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            // Используем метод authenticate из SecureTokenService
            Optional<User> user = tokenService.authenticate(request.login(), request.password());

            if (user.isPresent()) {
                String token = tokenService.createToken(user.get());
                return ResponseEntity.ok(new LoginResponse(token));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Неверные учетные данные", 400));
            }
        } catch (Exception e) {
            log.error("Ошибка при аутентификации", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Неверные учетные данные", 400));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("auth-token") String authToken) {
        tokenService.logout(authToken);
        return ResponseEntity.ok().build();
    }
}