package ru.netology.cloudservise.dto;

import jakarta.validation.constraints.NotBlank;


public record RenameFileRequest(
        @NotBlank(message = "Новое имя файла обязательно")
        String name
) {}