package ru.netology.cloudservise.unit;

import ru.netology.cloudservise.entity.User;
import ru.netology.cloudservise.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.netology.cloudservise.service.UserService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;
    private User testUser;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);

        testUser = new User();
        testUser.setId(1L);
        testUser.setLogin("testuser");
        testUser.setPassword("$2a$12$hashedPassword");
    }

    @Test
    void findByLogin_UserExists_ReturnsUser() {

        when(userRepository.findByLogin("testuser")).thenReturn(Optional.of(testUser));


        Optional<User> result = userService.findByLogin("testuser");


        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getLogin());
        verify(userRepository).findByLogin("testuser");
    }

    @Test
    void findByLogin_UserNotExists_ReturnsEmpty() {

        when(userRepository.findByLogin("nonexistent")).thenReturn(Optional.empty());


        Optional<User> result = userService.findByLogin("nonexistent");


        assertFalse(result.isPresent());
        verify(userRepository).findByLogin("nonexistent");
    }

    @Test
    void userExists_UserExists_ReturnsTrue() {

        when(userRepository.existsByLogin("testuser")).thenReturn(true);


        boolean result = userService.userExists("testuser");


        assertTrue(result);
        verify(userRepository).existsByLogin("testuser");
    }

    @Test
    void userExists_UserNotExists_ReturnsFalse() {

        when(userRepository.existsByLogin("nonexistent")).thenReturn(false);


        boolean result = userService.userExists("nonexistent");


        assertFalse(result);
        verify(userRepository).existsByLogin("nonexistent");
    }

    @Test
    void createUser_ValidData_CreatesUserWithHashedPassword() {

        String login = "newuser";
        String password = "password";
        String hashedPassword = "$2a$12$hashedPassword";

        when(userRepository.existsByLogin(login)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn(hashedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });


        User result = userService.createUser(login, password);


        assertNotNull(result);
        assertEquals(login, result.getLogin());
        assertEquals(hashedPassword, result.getPassword());
        verify(passwordEncoder).encode(password);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_UserExists_ThrowsException() {

        String login = "existinguser";
        String password = "password";

        when(userRepository.existsByLogin(login)).thenReturn(true);


        assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(login, password);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUserWithHashedPassword_ValidData_CreatesUser() {

        String login = "newuser";
        String hashedPassword = "$2a$12$hashedPassword";

        when(userRepository.existsByLogin(login)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });


        User result = userService.createUserWithHashedPassword(login, hashedPassword);


        assertNotNull(result);
        assertEquals(login, result.getLogin());
        assertEquals(hashedPassword, result.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUserWithHashedPassword_NotHashedPassword_ThrowsException() {

        String login = "newuser";
        String plainPassword = "plainpassword";

        when(userRepository.existsByLogin(login)).thenReturn(false);


        assertThrows(IllegalArgumentException.class, () -> {
            userService.createUserWithHashedPassword(login, plainPassword);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUserWithHashedPassword_UserExists_ThrowsException() {

        String login = "existinguser";
        String hashedPassword = "$2a$12$hashedPassword";

        when(userRepository.existsByLogin(login)).thenReturn(true);


        assertThrows(IllegalArgumentException.class, () -> {
            userService.createUserWithHashedPassword(login, hashedPassword);
        });

        verify(userRepository, never()).save(any(User.class));
    }
}