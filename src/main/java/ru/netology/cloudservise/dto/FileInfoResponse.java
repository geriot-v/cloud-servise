package ru.netology.cloudservise.dto;


public record FileInfoResponse(
        String filename,
        Long size
) {}
