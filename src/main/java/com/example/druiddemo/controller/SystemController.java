package com.example.druiddemo.controller;

import com.example.druiddemo.dto.ApiResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/system")
public class SystemController {

    private final JdbcTemplate jdbcTemplate;

    public SystemController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        Integer database = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        return ApiResponse.ok(Map.of("application", "UP", "database", database != null ? "UP" : "DOWN"));
    }
}
