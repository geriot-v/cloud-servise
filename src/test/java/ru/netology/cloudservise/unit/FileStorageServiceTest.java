package ru.netology.cloudservise.unit;

import org.springframework.web.multipart.MultipartFile;
import ru.netology.cloudservise.entity.User;
import ru.netology.cloudservise.entity.UserFile;
import ru.netology.cloudservise.repository.UserFileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.netology.cloudservise.service.FileStorageService;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceTest {

    @Mock
    private UserFileRepository userFileRepository;

    @Mock
    private MultipartFile multipartFile;

    private FileStorageService fileStorageService;
    private User testUser;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService(userFileRepository);
        fileStorageService.storagePath = "./test-uploads";

        testUser = new User();
        testUser.setId(1L);
        testUser.setLogin("testuser");
    }

    @Test
    void getUserFiles_WithLimit_ReturnsLimitedFiles() {

        UserFile file1 = new UserFile();
        file1.setFilename("file1.txt");
        file1.setSize(100L);

        UserFile file2 = new UserFile();
        file2.setFilename("file2.txt");
        file2.setSize(200L);

        List<UserFile> files = Arrays.asList(file1, file2);
        when(userFileRepository.findByUserOrderByUploadedAtDesc(testUser)).thenReturn(files);


        var result = fileStorageService.getUserFiles(testUser, 1);

        assertEquals(1, result.size());
        assertEquals("file1.txt", result.get(0).filename());
        verify(userFileRepository).findByUserOrderByUploadedAtDesc(testUser);
    }

    @Test
    void fileExists_FileExists_ReturnsTrue() {

        when(userFileRepository.existsByUserAndFilename(testUser, "testfile.txt")).thenReturn(true);


        boolean result = fileStorageService.fileExists(testUser, "testfile.txt");


        assertTrue(result);
        verify(userFileRepository).existsByUserAndFilename(testUser, "testfile.txt");
    }

    @Test
    void fileExists_FileNotExists_ReturnsFalse() {

        when(userFileRepository.existsByUserAndFilename(testUser, "nonexistent.txt")).thenReturn(false);


        boolean result = fileStorageService.fileExists(testUser, "nonexistent.txt");


        assertFalse(result);
    }
}