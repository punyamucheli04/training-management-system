package com.trainingmanagement.repository;

import com.trainingmanagement.model.Training;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

/** Data access for the training table (JdbcTemplate). */
@Repository
public class TrainingRepository {

    /**
     * Two LEFT JOINs + GROUP BY: number of sessions and enrollments per training.
     * COUNT(DISTINCT ...) avoids counting a session once per enrolled student.
     */
    private static final String SELECT_TRAININGS = """
            SELECT t.Training_ID, t.Tr_Name, t.Tr_Type, t.Description, t.Duration_Hrs,
                   COUNT(DISTINCT ts.Session_ID) AS Session_Count,
                   COUNT(e.Student_ID)           AS Enrollment_Count
              FROM training t
              LEFT JOIN training_session   ts ON ts.Training_ID = t.Training_ID
              LEFT JOIN session_enrollment e  ON e.Session_ID   = ts.Session_ID
            """;

    private static final String GROUP_BY =
            " GROUP BY t.Training_ID, t.Tr_Name, t.Tr_Type, t.Description, t.Duration_Hrs ";

    private static final RowMapper<Training> ROW_MAPPER = (rs, rowNum) -> {
        Training t = new Training();
        t.setTrainingId(rs.getInt("Training_ID"));
        t.setTrName(rs.getString("Tr_Name"));
        t.setTrType(rs.getString("Tr_Type"));
        t.setDescription(rs.getString("Description"));
        t.setDurationHrs(RowUtils.getInteger(rs, "Duration_Hrs"));
        t.setSessionCount(rs.getInt("Session_Count"));
        t.setEnrollmentCount(rs.getInt("Enrollment_Count"));
        return t;
    };

    private final JdbcTemplate jdbcTemplate;

    public TrainingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Training> findAll(String search) {
        if (search == null || search.isBlank()) {
            return jdbcTemplate.query(SELECT_TRAININGS + GROUP_BY + " ORDER BY t.Training_ID", ROW_MAPPER);
        }
        String like = "%" + search.trim() + "%";
        return jdbcTemplate.query(
                SELECT_TRAININGS + " WHERE t.Tr_Name LIKE ? OR t.Tr_Type LIKE ? OR t.Description LIKE ? "
                        + GROUP_BY + " ORDER BY t.Training_ID",
                ROW_MAPPER, like, like, like);
    }

    public Optional<Training> findById(int trainingId) {
        return jdbcTemplate.query(SELECT_TRAININGS + " WHERE t.Training_ID = ? " + GROUP_BY, ROW_MAPPER, trainingId)
                .stream().findFirst();
    }

    public boolean existsById(int trainingId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM training WHERE Training_ID = ?", Integer.class, trainingId);
        return count != null && count > 0;
    }

    public int insert(Training t) {
        String sql = "INSERT INTO training (Tr_Name, Tr_Type, Description, Duration_Hrs) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, t.getTrName());
            ps.setString(2, t.getTrType());
            ps.setString(3, t.getDescription());
            ps.setObject(4, t.getDurationHrs());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().intValue();
    }

    public int update(int trainingId, Training t) {
        return jdbcTemplate.update(
                "UPDATE training SET Tr_Name = ?, Tr_Type = ?, Description = ?, Duration_Hrs = ? WHERE Training_ID = ?",
                t.getTrName(), t.getTrType(), t.getDescription(), t.getDurationHrs(), trainingId);
    }

    public int delete(int trainingId) {
        return jdbcTemplate.update("DELETE FROM training WHERE Training_ID = ?", trainingId);
    }

    public int countSessions(int trainingId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM training_session WHERE Training_ID = ?", Integer.class, trainingId);
        return count == null ? 0 : count;
    }
}
