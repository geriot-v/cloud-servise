package ru.netology.cloudservise.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.netology.cloudservise.config.TestSecurityConfig;
import ru.netology.cloudservise.dto.LoginRequest;
import ru.netology.cloudservise.entity.User;
import ru.netology.cloudservise.security.SecureTokenService;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SecureTokenService tokenService;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    void login_ValidCredentials_ReturnsToken() throws Exception {
        LoginRequest request = new LoginRequest("user1", "password");
        User user = new User();
        user.setLogin("user1");

        when(tokenService.authenticate("user1", "password")).thenReturn(Optional.of(user));
        when(tokenService.createToken(user)).thenReturn("test-token");

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authToken").value("test-token"));
    }

    @Test
    void login_InvalidCredentials_ReturnsError() throws Exception {
        LoginRequest request = new LoginRequest("user1", "wrongpassword");

        when(tokenService.authenticate("user1", "wrongpassword")).thenReturn(Optional.empty());

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Неверные учетные данные"));
    }
}