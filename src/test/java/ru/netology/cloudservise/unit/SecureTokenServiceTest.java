package ru.netology.cloudservise.unit;

import org.mockito.stubbing.OngoingStubbing;
import ru.netology.cloudservise.entity.AuthToken;
import ru.netology.cloudservise.entity.User;
import ru.netology.cloudservise.repository.AuthTokenRepository;
import ru.netology.cloudservise.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.netology.cloudservise.security.SecureTokenService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class SecureTokenServiceTest {

    @Mock
    private AuthTokenRepository authTokenRepository;

    @Mock
    private UserRepository userRepository;

    private SecureTokenService tokenService;
    private User testUser;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        tokenService = new SecureTokenService(authTokenRepository, userRepository, passwordEncoder);

        testUser = new User();
        testUser.setId(1L);
        testUser.setLogin("testuser");
        testUser.setPassword(passwordEncoder.encode("password123"));
    }

    @Test
    void loadUserByUsername_UserExists_ReturnsUserDetails() {

        when(userRepository.findByLogin("testuser")).thenReturn(Optional.of(testUser));


        UserDetails userDetails = tokenService.loadUserByUsername("testuser");


        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
        verify(userRepository).findByLogin("testuser");
    }

    @Test
    void loadUserByUsername_UserNotFound_ThrowsException() {

        when(userRepository.findByLogin("nonexistent")).thenReturn(Optional.empty());


        assertThrows(UsernameNotFoundException.class, () -> {
            tokenService.loadUserByUsername("nonexistent");
        });

        verify(userRepository).findByLogin("nonexistent");
    }

    @Test
    void authenticate_ValidCredentials_ReturnsUser() {

        when(userRepository.findByLogin("testuser")).thenReturn(Optional.of(testUser));


        Optional<User> result = tokenService.authenticate("testuser", "password123");


        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getLogin());
        verify(userRepository).findByLogin("testuser");
    }

    @Test
    void authenticate_InvalidPassword_ReturnsEmpty() {

        when(userRepository.findByLogin("testuser")).thenReturn(Optional.of(testUser));


        Optional<User> result = tokenService.authenticate("testuser", "wrongpassword");


        assertFalse(result.isPresent());
        verify(userRepository).findByLogin("testuser");
    }

    @Test
    void authenticate_UserNotFound_ReturnsEmpty() {

        when(userRepository.findByLogin("nonexistent")).thenReturn(Optional.empty());


        Optional<User> result = tokenService.authenticate("nonexistent", "password");


        assertFalse(result.isPresent());
        verify(userRepository).findByLogin("nonexistent");
    }

    @Test
    void createToken_ValidUser_ReturnsToken() {

        OngoingStubbing<AuthToken> authTokenOngoingStubbing = when(authTokenRepository.save(any(AuthToken.class))).thenAnswer(invocation -> {
            AuthToken token = invocation.getArgument(0);
            token.setId(1L);
            return token;
        });


        String token = tokenService.createToken(testUser);


        assertNotNull(token);
        assertTrue(token.length() > 0);
        verify(authTokenRepository).save(any(AuthToken.class));
    }

    @Test
    void validateToken_ValidToken_ReturnsUser() {

        String rawToken = "valid_token_123";
        String tokenHash = passwordEncoder.encode(rawToken);

        AuthToken authToken = new AuthToken();
        authToken.setTokenHash(tokenHash);
        authToken.setUser(testUser);
        authToken.setExpiresAt(LocalDateTime.now().plusHours(24));

        List<AuthToken> activeTokens = Arrays.asList(authToken);
        when(authTokenRepository.findAllActiveTokens(any(LocalDateTime.class))).thenReturn(activeTokens);


        Optional<User> result = tokenService.validateToken(rawToken);

        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
        verify(authTokenRepository).findAllActiveTokens(any(LocalDateTime.class));
    }

    @Test
    void validateToken_InvalidToken_ReturnsEmpty() {

        String rawToken = "invalid_token";
        String tokenHash = passwordEncoder.encode("different_token");

        AuthToken authToken = new AuthToken();
        authToken.setTokenHash(tokenHash);
        authToken.setUser(testUser);
        authToken.setExpiresAt(LocalDateTime.now().plusHours(24));

        List<AuthToken> activeTokens = Arrays.asList(authToken);
        when(authTokenRepository.findAllActiveTokens(any(LocalDateTime.class))).thenReturn(activeTokens);


        Optional<User> result = tokenService.validateToken(rawToken);


        assertFalse(result.isPresent());
        verify(authTokenRepository).findAllActiveTokens(any(LocalDateTime.class));
    }

    @Test
    void validateToken_ExpiredToken_ReturnsEmpty() {

        String rawToken = "expired_token";
        String tokenHash = passwordEncoder.encode(rawToken);

        AuthToken authToken = new AuthToken();
        authToken.setTokenHash(tokenHash);
        authToken.setUser(testUser);
        authToken.setExpiresAt(LocalDateTime.now().minusHours(1)); // Просроченный

        List<AuthToken> activeTokens = Arrays.asList();
        when(authTokenRepository.findAllActiveTokens(any(LocalDateTime.class))).thenReturn(activeTokens);


        Optional<User> result = tokenService.validateToken(rawToken);


        assertFalse(result.isPresent());
        verify(authTokenRepository).findAllActiveTokens(any(LocalDateTime.class));
    }

    @Test
    void validateToken_NullToken_ReturnsEmpty() {

        Optional<User> result = tokenService.validateToken(null);


        assertFalse(result.isPresent());
        verify(authTokenRepository, never()).findAllActiveTokens(any(LocalDateTime.class));
    }

    @Test
    void validateToken_EmptyToken_ReturnsEmpty() {

        Optional<User> result = tokenService.validateToken("");


        assertFalse(result.isPresent());
        verify(authTokenRepository, never()).findAllActiveTokens(any(LocalDateTime.class));
    }

    @Test
    void logout_ValidToken_DeletesToken() {

        String rawToken = "valid_token";
        String tokenHash = passwordEncoder.encode(rawToken);

        AuthToken authToken = new AuthToken();
        authToken.setTokenHash(tokenHash);
        authToken.setUser(testUser);
        authToken.setExpiresAt(LocalDateTime.now().plusHours(24));

        List<AuthToken> activeTokens = Arrays.asList(authToken);
        when(authTokenRepository.findAllActiveTokens(any(LocalDateTime.class))).thenReturn(activeTokens);


        tokenService.logout(rawToken);


        verify(authTokenRepository).delete(authToken);
    }

    @Test
    void logout_InvalidToken_DoesNothing() {

        String rawToken = "invalid_token";

        List<AuthToken> activeTokens = Arrays.asList();
        when(authTokenRepository.findAllActiveTokens(any(LocalDateTime.class))).thenReturn(activeTokens);


        tokenService.logout(rawToken);


        verify(authTokenRepository, never()).delete(any(AuthToken.class));
    }

    @Test
    void logout_NullToken_DoesNothing() {

        tokenService.logout(null);


        verify(authTokenRepository, never()).findAllActiveTokens(any(LocalDateTime.class));
        verify(authTokenRepository, never()).delete(any(AuthToken.class));
    }

    @Test
    void cleanupExpiredTokens_CallsRepository() {

        when(authTokenRepository.deleteExpiredTokens(any(LocalDateTime.class))).thenReturn(5);


        tokenService.cleanupExpiredTokens();


        verify(authTokenRepository).deleteExpiredTokens(any(LocalDateTime.class));
    }

    @Test
    void cleanupExpiredTokens_Exception_LogsError() {

        when(authTokenRepository.deleteExpiredTokens(any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("Database error"));


        assertDoesNotThrow(() -> {
            tokenService.cleanupExpiredTokens();
        });


        verify(authTokenRepository).deleteExpiredTokens(any(LocalDateTime.class));
    }
}