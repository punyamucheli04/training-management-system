package com.trainingmanagement.repository;

import com.trainingmanagement.model.Trainer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

/** Data access for the trainer table (JdbcTemplate). */
@Repository
public class TrainerRepository {

    /** Correlated subquery counts the sessions taken by each trainer. */
    private static final String SELECT_TRAINERS = """
            SELECT tr.Trainer_ID, tr.Trainer_Name, tr.Email, tr.Expertise,
                   (SELECT COUNT(*) FROM training_session ts
                     WHERE ts.Trainer_ID = tr.Trainer_ID) AS Session_Count
              FROM trainer tr
            """;

    private static final RowMapper<Trainer> ROW_MAPPER = (rs, rowNum) -> {
        Trainer t = new Trainer();
        t.setTrainerId(rs.getInt("Trainer_ID"));
        t.setTrainerName(rs.getString("Trainer_Name"));
        t.setEmail(rs.getString("Email"));
        t.setExpertise(rs.getString("Expertise"));
        t.setSessionCount(rs.getInt("Session_Count"));
        return t;
    };

    private final JdbcTemplate jdbcTemplate;

    public TrainerRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Trainer> findAll(String search) {
        if (search == null || search.isBlank()) {
            return jdbcTemplate.query(SELECT_TRAINERS + " ORDER BY tr.Trainer_ID", ROW_MAPPER);
        }
        String like = "%" + search.trim() + "%";
        return jdbcTemplate.query(
                SELECT_TRAINERS + " WHERE tr.Trainer_Name LIKE ? OR tr.Email LIKE ? OR tr.Expertise LIKE ?"
                        + " ORDER BY tr.Trainer_ID",
                ROW_MAPPER, like, like, like);
    }

    public Optional<Trainer> findById(int trainerId) {
        return jdbcTemplate.query(SELECT_TRAINERS + " WHERE tr.Trainer_ID = ?", ROW_MAPPER, trainerId)
                .stream().findFirst();
    }

    public boolean existsById(int trainerId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM trainer WHERE Trainer_ID = ?", Integer.class, trainerId);
        return count != null && count > 0;
    }

    public boolean emailInUse(String email, Integer excludeId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM trainer WHERE Email = ? AND Trainer_ID <> ?",
                Integer.class, email, excludeId == null ? -1 : excludeId);
        return count != null && count > 0;
    }

    public int insert(Trainer t) {
        String sql = "INSERT INTO trainer (Trainer_Name, Email, Expertise) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, t.getTrainerName());
            ps.setString(2, t.getEmail());
            ps.setString(3, t.getExpertise());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().intValue();
    }

    public int update(int trainerId, Trainer t) {
        return jdbcTemplate.update(
                "UPDATE trainer SET Trainer_Name = ?, Email = ?, Expertise = ? WHERE Trainer_ID = ?",
                t.getTrainerName(), t.getEmail(), t.getExpertise(), trainerId);
    }

    public int delete(int trainerId) {
        return jdbcTemplate.update("DELETE FROM trainer WHERE Trainer_ID = ?", trainerId);
    }

    public int countSessions(int trainerId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM training_session WHERE Trainer_ID = ?", Integer.class, trainerId);
        return count == null ? 0 : count;
    }
}
