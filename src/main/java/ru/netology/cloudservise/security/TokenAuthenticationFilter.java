package ru.netology.cloudservise.security;

import ru.netology.cloudservise.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;


@Slf4j
@Component
@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    // Инжектим только SecureTokenService, убираем циклическую зависимость
    private final SecureTokenService tokenService;

    @Override
    public void doFilterInternal(HttpServletRequest request,
                                 HttpServletResponse response,
                                 FilterChain filterChain) throws ServletException, IOException {

        if (request.getServletPath().equals("/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authToken = extractTokenFromHeader(request);

        if (authToken != null) {
            try {
                Optional<User> userOptional = tokenService.validateToken(authToken);

                if (userOptional.isPresent()) {
                    User user = userOptional.get();

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    user,
                                    null,
                                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                            );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("Пользователь аутентифицирован: {}", user.getLogin());
                } else {
                    log.warn("Невалидный токен аутентификации");
                }
            } catch (Exception e) {
                log.error("Ошибка при аутентификации токена", e);
            }
        } else {
            log.debug("Токен аутентификации отсутствует в запросе");
        }

        filterChain.doFilter(request, response);
    }

    public String extractTokenFromHeader(HttpServletRequest request) {
        String authHeader = request.getHeader("auth-token");

        if (authHeader != null && !authHeader.trim().isEmpty()) {
            return authHeader.trim();
        }

        return null;
    }
}