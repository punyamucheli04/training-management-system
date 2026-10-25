package com.trainingmanagement.repository;

import com.trainingmanagement.model.UserAccount;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/** Data access for the existing users table (JdbcTemplate). */
@Repository
public class UserRepository {

    /** A users row including the password hash - used only inside the backend. */
    public record UserRow(UserAccount account, String passwordHash) {
    }

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<UserRow> findByUsername(String username) {
        return jdbcTemplate.query(
                "SELECT User_ID, Username, Email, Password_Hash, Role FROM users WHERE Username = ?",
                (rs, rowNum) -> new UserRow(
                        new UserAccount(rs.getInt("User_ID"), rs.getString("Username"),
                                rs.getString("Email"), rs.getString("Role")),
                        rs.getString("Password_Hash")),
                username).stream().findFirst();
    }

    public int updatePasswordHash(int userId, String passwordHash) {
        return jdbcTemplate.update("UPDATE users SET Password_Hash = ? WHERE User_ID = ?", passwordHash, userId);
    }
}
