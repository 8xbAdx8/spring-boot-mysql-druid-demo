package com.example.druiddemo.repository;

import com.example.druiddemo.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {

    private static final RowMapper<User> USER_ROW_MAPPER = (resultSet, rowNum) -> new User(
            resultSet.getLong("id"),
            resultSet.getString("name"),
            resultSet.getString("email"),
            resultSet.getString("status"),
            resultSet.getTimestamp("created_at").toLocalDateTime(),
            resultSet.getTimestamp("updated_at").toLocalDateTime()
    );

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public User save(String name, String email, String status) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO app_user(name, email, status) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, status);
            return statement;
        }, keyHolder);
        return findById(keyHolder.getKey().longValue()).orElseThrow();
    }

    public Optional<User> findById(long id) {
        return jdbcTemplate.query("SELECT * FROM app_user WHERE id = ?", USER_ROW_MAPPER, id)
                .stream().findFirst();
    }

    public List<User> findPage(String keyword, int offset, int size) {
        String search = "%" + keyword + "%";
        return jdbcTemplate.query("""
                SELECT * FROM app_user
                WHERE name LIKE ? OR email LIKE ?
                ORDER BY id DESC
                LIMIT ? OFFSET ?
                """, USER_ROW_MAPPER, search, search, size, offset);
    }

    public long count(String keyword) {
        String search = "%" + keyword + "%";
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM app_user WHERE name LIKE ? OR email LIKE ?",
                Long.class, search, search
        );
        return count == null ? 0 : count;
    }

    public boolean existsByEmail(String email, Long excludedId) {
        String sql = excludedId == null
                ? "SELECT COUNT(*) FROM app_user WHERE email = ?"
                : "SELECT COUNT(*) FROM app_user WHERE email = ? AND id <> ?";
        Integer count = excludedId == null
                ? jdbcTemplate.queryForObject(sql, Integer.class, email)
                : jdbcTemplate.queryForObject(sql, Integer.class, email, excludedId);
        return count != null && count > 0;
    }

    public User update(long id, String name, String email, String status) {
        jdbcTemplate.update("""
                UPDATE app_user
                SET name = ?, email = ?, status = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """, name, email, status, id);
        return findById(id).orElseThrow();
    }

    public int deleteById(long id) {
        return jdbcTemplate.update("DELETE FROM app_user WHERE id = ?", id);
    }
}
