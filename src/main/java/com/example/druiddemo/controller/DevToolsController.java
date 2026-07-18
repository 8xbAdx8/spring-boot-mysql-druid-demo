package com.example.druiddemo.controller;

import com.example.druiddemo.dto.ApiResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("dev")
@RestController
@RequestMapping("/api/dev")
public class DevToolsController {

    private final JdbcTemplate jdbcTemplate;

    public DevToolsController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/slow-sql")
    public ApiResponse<String> slowSql() {
        jdbcTemplate.queryForObject("SELECT SLEEP(1.2)", Integer.class);
        return ApiResponse.ok("慢 SQL 已执行，请在 Druid 的 SQL 监控中查看");
    }
}
