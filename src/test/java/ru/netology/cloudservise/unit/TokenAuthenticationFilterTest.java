package ru.netology.cloudservise.unit;

import ru.netology.cloudservise.entity.User;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.netology.cloudservise.security.SecureTokenService;
import ru.netology.cloudservise.security.TokenAuthenticationFilter;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class TokenAuthenticationFilterTest {

    @Mock
    private SecureTokenService tokenService;

    private TokenAuthenticationFilter tokenAuthenticationFilter;

    @BeforeEach
    void setUp() {
        tokenAuthenticationFilter = new TokenAuthenticationFilter(tokenService);
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_ValidToken_SetsAuthentication() throws ServletException, IOException {

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        String validToken = "valid_token";
        User user = new User();
        user.setLogin("testuser");

        request.addHeader("auth-token", validToken);
        when(tokenService.validateToken(validToken)).thenReturn(Optional.of(user));


        tokenAuthenticationFilter.doFilterInternal(request, response, (req, res) -> {});


        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(user, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        verify(tokenService).validateToken(validToken);
    }

    @Test
    void doFilterInternal_InvalidToken_NoAuthenticationSet() throws ServletException, IOException {

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        String invalidToken = "invalid_token";
        request.addHeader("auth-token", invalidToken);
        when(tokenService.validateToken(invalidToken)).thenReturn(Optional.empty());


        tokenAuthenticationFilter.doFilterInternal(request, response, (req, res) -> {});

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(tokenService).validateToken(invalidToken);
    }

    @Test
    void doFilterInternal_NoToken_NoAuthenticationSet() throws ServletException, IOException {

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();


        tokenAuthenticationFilter.doFilterInternal(request, response, (req, res) -> {});


        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(tokenService, never()).validateToken(anyString());
    }

    @Test
    void doFilterInternal_LoginEndpoint_SkipsAuthentication() throws ServletException, IOException {

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.setServletPath("/login");


        tokenAuthenticationFilter.doFilterInternal(request, response, (req, res) -> {});


        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(tokenService, never()).validateToken(anyString());
    }

    @Test
    void doFilterInternal_ExceptionDuringValidation_NoAuthenticationSet() throws ServletException, IOException {

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        String token = "problematic_token";
        request.addHeader("auth-token", token);
        when(tokenService.validateToken(token)).thenThrow(new RuntimeException("Validation error"));


        tokenAuthenticationFilter.doFilterInternal(request, response, (req, res) -> {});


        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(tokenService).validateToken(token);
    }

    @Test
    void extractTokenFromHeader_ValidHeader_ReturnsToken() {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("auth-token", "test_token");


        String token = tokenAuthenticationFilter.extractTokenFromHeader(request);


        assertEquals("test_token", token);
    }

    @Test
    void extractTokenFromHeader_EmptyHeader_ReturnsNull() {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("auth-token", "");


        String token = tokenAuthenticationFilter.extractTokenFromHeader(request);


        assertNull(token);
    }

    @Test
    void extractTokenFromHeader_WhitespaceHeader_ReturnsNull() {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("auth-token", "   ");


        String token = tokenAuthenticationFilter.extractTokenFromHeader(request);


        assertNull(token);
    }

    @Test
    void extractTokenFromHeader_NoHeader_ReturnsNull() {

        MockHttpServletRequest request = new MockHttpServletRequest();


        String token = tokenAuthenticationFilter.extractTokenFromHeader(request);


        assertNull(token);
    }
}