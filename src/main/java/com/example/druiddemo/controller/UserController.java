package com.example.druiddemo.controller;

import com.example.druiddemo.dto.ApiResponse;
import com.example.druiddemo.dto.PageResponse;
import com.example.druiddemo.dto.UserRequest;
import com.example.druiddemo.model.User;
import com.example.druiddemo.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<User>> create(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(userService.create(request)));
    }

    @GetMapping("/{id}")
    public ApiResponse<User> get(@PathVariable long id) {
        return ApiResponse.ok(userService.get(id));
    }

    @GetMapping
    public ApiResponse<PageResponse<User>> list(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(userService.list(keyword, page, size));
    }

    @PutMapping("/{id}")
    public ApiResponse<User> update(@PathVariable long id, @Valid @RequestBody UserRequest request) {
        return ApiResponse.ok(userService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
