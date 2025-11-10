package ru.netology.cloudservise.service;

import ru.netology.cloudservise.dto.FileInfoResponse;
import ru.netology.cloudservise.entity.User;
import ru.netology.cloudservise.entity.UserFile;
import ru.netology.cloudservise.repository.UserFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Сервис для управления файлами пользователей.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final UserFileRepository userFileRepository;

    @Value("${app.file-storage.path}")
    public String storagePath;

    public void storeFile(User user, String filename, MultipartFile file) throws IOException {
        Path userDir = Paths.get(storagePath, user.getId().toString());
        Files.createDirectories(userDir);

        Path filePath = userDir.resolve(filename);
        Files.write(filePath, file.getBytes());

        UserFile userFile = new UserFile();
        userFile.setUser(user);
        userFile.setFilename(filename);
        userFile.setSize(file.getSize());
        userFile.setStoragePath(filePath.toString());

        userFileRepository.save(userFile);
        log.info("Файл успешно сохранен: {} для пользователя {}", filename, user.getLogin());
    }

    public byte[] loadFile(User user, String filename) throws IOException {
        UserFile userFile = userFileRepository.findByUserAndFilename(user, filename)
                .orElseThrow(() -> new RuntimeException("Файл не найден"));

        return Files.readAllBytes(Paths.get(userFile.getStoragePath()));
    }

    public void deleteFile(User user, String filename) throws IOException {
        UserFile userFile = userFileRepository.findByUserAndFilename(user, filename)
                .orElseThrow(() -> new RuntimeException("Файл не найден"));

        Files.deleteIfExists(Paths.get(userFile.getStoragePath()));
        userFileRepository.delete(userFile);
        log.info("Файл успешно удален: {} для пользователя {}", filename, user.getLogin());
    }

    public void renameFile(User user, String oldFilename, String newFilename) throws IOException {
        UserFile userFile = userFileRepository.findByUserAndFilename(user, oldFilename)
                .orElseThrow(() -> new RuntimeException("Файл не найден"));

        if (userFileRepository.existsByUserAndFilename(user, newFilename)) {
            throw new RuntimeException("Файл с новым именем уже существует");
        }

        Path oldPath = Paths.get(userFile.getStoragePath());
        Path newPath = oldPath.resolveSibling(newFilename);
        Files.move(oldPath, newPath);

        userFile.setFilename(newFilename);
        userFile.setStoragePath(newPath.toString());
        userFileRepository.save(userFile);
        log.info("Файл переименован с {} на {} для пользователя {}", oldFilename, newFilename, user.getLogin());
    }

    public List<FileInfoResponse> getUserFiles(User user, Integer limit) {
        List<UserFile> files = userFileRepository.findByUserOrderByUploadedAtDesc(user);

        if (limit != null && limit > 0) {
            files = files.stream().limit(limit).toList();
        }

        return files.stream()
                .map(file -> new FileInfoResponse(file.getFilename(), file.getSize()))
                .toList();
    }

    public boolean fileExists(User user, String filename) {
        return userFileRepository.existsByUserAndFilename(user, filename);
    }
}
