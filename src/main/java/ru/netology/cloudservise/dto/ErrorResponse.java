package ru.netology.cloudservise.dto;


public record ErrorResponse(
        String message,
        Integer id
) {
    public ErrorResponse(String message) {
        this(message, -1);
    }
}