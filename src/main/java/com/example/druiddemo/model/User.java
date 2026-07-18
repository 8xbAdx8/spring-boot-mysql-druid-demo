package com.example.druiddemo.model;

import java.time.LocalDateTime;

public record User(
        Long id,
        String name,
        String email,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
