package com.example.druiddemo.service;

import com.example.druiddemo.dto.PageResponse;
import com.example.druiddemo.dto.UserRequest;
import com.example.druiddemo.exception.ConflictException;
import com.example.druiddemo.exception.NotFoundException;
import com.example.druiddemo.model.User;
import com.example.druiddemo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User create(UserRequest request) {
        String email = normalizeEmail(request.email());
        ensureEmailAvailable(email, null);
        return userRepository.save(request.name().trim(), email, request.normalizedStatus());
    }

    @Transactional(readOnly = true)
    public User get(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("用户不存在：" + id));
    }

    @Transactional(readOnly = true)
    public PageResponse<User> list(String keyword, int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("page 不能小于 0，size 必须在 1 到 100 之间");
        }
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        return PageResponse.of(
                userRepository.findPage(normalizedKeyword, page * size, size),
                page,
                size,
                userRepository.count(normalizedKeyword)
        );
    }

    @Transactional
    public User update(long id, UserRequest request) {
        get(id);
        String email = normalizeEmail(request.email());
        ensureEmailAvailable(email, id);
        return userRepository.update(id, request.name().trim(), email, request.normalizedStatus());
    }

    @Transactional
    public void delete(long id) {
        if (userRepository.deleteById(id) == 0) {
            throw new NotFoundException("用户不存在：" + id);
        }
    }

    private void ensureEmailAvailable(String email, Long excludedId) {
        if (userRepository.existsByEmail(email, excludedId)) {
            throw new ConflictException("邮箱已被使用：" + email);
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
