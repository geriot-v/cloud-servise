package ru.netology.cloudservise.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import ru.netology.cloudservise.dto.LoginRequest;
import ru.netology.cloudservise.entity.User;
import ru.netology.cloudservise.repository.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never",
        "app.file-storage.path=./test-uploads"
})
class FileStorageIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();

        // Создаем тестового пользователя
        User testUser = new User();
        testUser.setLogin("testuser");
        testUser.setPassword(passwordEncoder.encode("testpass123"));
        userRepository.save(testUser);

        LoginRequest loginRequest = new LoginRequest("testuser", "testpass123");

        MvcResult result = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        authToken = JsonPath.read(response, "$.authToken");
    }

    @Test
    void uploadAndDownloadFile_Success() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "file content".getBytes()
        );

        mockMvc.perform(multipart("/file")
                        .file(file)
                        .param("filename", "test.txt")
                        .header("auth-token", authToken))
                .andExpect(status().isOk());


        mockMvc.perform(get("/list")
                        .header("auth-token", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].filename").value("test.txt"));


        mockMvc.perform(get("/file")
                        .param("filename", "test.txt")
                        .header("auth-token", authToken))
                .andExpect(status().isOk())
                .andExpect(content().bytes("file content".getBytes()));
    }
}