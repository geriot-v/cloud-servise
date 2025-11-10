package ru.netology.cloudservise.controller;

import ru.netology.cloudservise.dto.ErrorResponse;
import ru.netology.cloudservise.dto.FileInfoResponse;
import ru.netology.cloudservise.dto.RenameFileRequest;
import ru.netology.cloudservise.entity.User;
import ru.netology.cloudservise.security.SecureTokenService;
import ru.netology.cloudservise.service.FileStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Optional;


@RestController
@RequiredArgsConstructor
public class FileController {

    private final SecureTokenService tokenService;
    private final FileStorageService fileStorageService;

    @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(
            @RequestHeader("auth-token") String authToken,
            @RequestParam("filename") String filename,
            @RequestPart("file") MultipartFile file) {

        Optional<User> user = tokenService.validateToken(authToken);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Ошибка авторизации", 401));
        }

        if (filename == null || filename.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Ошибка входных данных", 400));
        }

        try {
            if (fileStorageService.fileExists(user.get(), filename)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Файл уже существует", 400));
            }

            fileStorageService.storeFile(user.get(), filename, file);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Ошибка входных данных", 400));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Ошибка загрузки файла", 500));
        }
    }

    @GetMapping("/file")
    public ResponseEntity<?> downloadFile(
            @RequestHeader("auth-token") String authToken,
            @RequestParam("filename") String filename) {

        Optional<User> user = tokenService.validateToken(authToken);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Ошибка авторизации", 401));
        }

        try {
            byte[] fileContent = fileStorageService.loadFile(user.get(), filename);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileContent);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Ошибка входных данных", 400));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Ошибка загрузки файла", 500));
        }
    }

    @DeleteMapping("/file")
    public ResponseEntity<?> deleteFile(
            @RequestHeader("auth-token") String authToken,
            @RequestParam("filename") String filename) {

        Optional<User> user = tokenService.validateToken(authToken);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Ошибка авторизации", 401));
        }

        try {
            fileStorageService.deleteFile(user.get(), filename);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Ошибка входных данных", 400));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Ошибка удаления файла", 500));
        }
    }

    @PutMapping("/file")
    public ResponseEntity<?> renameFile(
            @RequestHeader("auth-token") String authToken,
            @RequestParam("filename") String filename,
            @Valid @RequestBody RenameFileRequest request) {

        Optional<User> user = tokenService.validateToken(authToken);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Ошибка авторизации", 401));
        }

        try {
            fileStorageService.renameFile(user.get(), filename, request.name());
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Ошибка входных данных", 400));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Ошибка загрузки файла", 500));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> getFileList(
            @RequestHeader("auth-token") String authToken,
            @RequestParam(value = "limit", required = false) Integer limit) {

        Optional<User> user = tokenService.validateToken(authToken);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Ошибка авторизации", 401));
        }

        try {
            List<FileInfoResponse> files = fileStorageService.getUserFiles(user.get(), limit);
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Ошибка получения списка файлов", 500));
        }
    }
}