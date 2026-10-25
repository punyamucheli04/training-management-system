package com.trainingmanagement.repository;

import com.trainingmanagement.model.TrainingSession;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Data access for the training_session table.
 * Reads use the VIEW session_details_view (session JOIN training JOIN trainer).
 */
@Repository
public class SessionRepository {

    private static final String SELECT_SESSIONS = """
            SELECT Session_ID, Session_Date, Start_Time, End_Time,
                   Training_ID, Tr_Name, Tr_Type, Trainer_ID, Trainer_Name,
                   Enrolled_Count, Present_Count, Session_Status
              FROM session_details_view
            """;

    static final RowMapper<TrainingSession> ROW_MAPPER = (rs, rowNum) -> {
        TrainingSession s = new TrainingSession();
        s.setSessionId(rs.getInt("Session_ID"));
        s.setSessionDate(RowUtils.getDate(rs, "Session_Date"));
        s.setStartTime(RowUtils.getTime(rs, "Start_Time"));
        s.setEndTime(RowUtils.getTime(rs, "End_Time"));
        s.setTrainingId(rs.getInt("Training_ID"));
        s.setTrName(rs.getString("Tr_Name"));
        s.setTrType(rs.getString("Tr_Type"));
        s.setTrainerId(rs.getInt("Trainer_ID"));
        s.setTrainerName(rs.getString("Trainer_Name"));
        s.setEnrolledCount(rs.getInt("Enrolled_Count"));
        s.setPresentCount(rs.getInt("Present_Count"));
        s.setSessionStatus(rs.getString("Session_Status"));
        return s;
    };

    private final JdbcTemplate jdbcTemplate;

    public SessionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * @param status   optional: "upcoming" (today and later) or "completed" (before today)
     * @param search   optional text matched against training and trainer name
     */
    public List<TrainingSession> findAll(String status, String search) {
        StringBuilder sql = new StringBuilder(SELECT_SESSIONS).append(" WHERE 1 = 1");
        List<Object> params = new ArrayList<>();

        if ("upcoming".equalsIgnoreCase(status)) {
            sql.append(" AND Session_Date >= CURDATE()");
        } else if ("completed".equalsIgnoreCase(status)) {
            sql.append(" AND Session_Date < CURDATE()");
        }
        if (search != null && !search.isBlank()) {
            sql.append(" AND (Tr_Name LIKE ? OR Trainer_Name LIKE ?)");
            String like = "%" + search.trim() + "%";
            params.add(like);
            params.add(like);
        }
        if ("upcoming".equalsIgnoreCase(status)) {
            sql.append(" ORDER BY Session_Date, Start_Time");
        } else {
            sql.append(" ORDER BY Session_Date DESC, Start_Time DESC");
        }
        return jdbcTemplate.query(sql.toString(), ROW_MAPPER, params.toArray());
    }

    public List<TrainingSession> findUpcoming(int limit) {
        return jdbcTemplate.query(
                SELECT_SESSIONS + " WHERE Session_Date >= CURDATE() ORDER BY Session_Date, Start_Time LIMIT ?",
                ROW_MAPPER, limit);
    }

    public Optional<TrainingSession> findById(int sessionId) {
        return jdbcTemplate.query(SELECT_SESSIONS + " WHERE Session_ID = ?", ROW_MAPPER, sessionId)
                .stream().findFirst();
    }

    public boolean existsById(int sessionId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM training_session WHERE Session_ID = ?", Integer.class, sessionId);
        return count != null && count > 0;
    }

    public int insert(TrainingSession s) {
        String sql = "INSERT INTO training_session (Session_Date, Start_Time, End_Time, Training_ID, Trainer_ID) "
                + "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, s.getSessionDate());
            ps.setObject(2, s.getStartTime());
            ps.setObject(3, s.getEndTime());
            ps.setInt(4, s.getTrainingId());
            ps.setInt(5, s.getTrainerId());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().intValue();
    }

    public int update(int sessionId, TrainingSession s) {
        return jdbcTemplate.update(
                "UPDATE training_session SET Session_Date = ?, Start_Time = ?, End_Time = ?, Training_ID = ?, Trainer_ID = ? "
                        + "WHERE Session_ID = ?",
                s.getSessionDate(), s.getStartTime(), s.getEndTime(), s.getTrainingId(), s.getTrainerId(), sessionId);
    }

    public int delete(int sessionId) {
        return jdbcTemplate.update("DELETE FROM training_session WHERE Session_ID = ?", sessionId);
    }

    /**
     * Calls the STORED PROCEDURE GetSessionAttendanceSummary with one IN and
     * three OUT parameters, using a JDBC CallableStatement.
     */
    public Map<String, Object> attendanceSummary(int sessionId) {
        return jdbcTemplate.execute(
                connection -> connection.prepareCall("{call GetSessionAttendanceSummary(?, ?, ?, ?)}"),
                (CallableStatementCallback<Map<String, Object>>) (CallableStatement cs) -> {
                    cs.setInt(1, sessionId);
                    cs.registerOutParameter(2, Types.INTEGER);
                    cs.registerOutParameter(3, Types.INTEGER);
                    cs.registerOutParameter(4, Types.INTEGER);
                    cs.execute();

                    Map<String, Object> summary = new LinkedHashMap<>();
                    summary.put("Session_ID", sessionId);
                    summary.put("Present_Count", cs.getInt(2));
                    summary.put("Absent_Count", cs.getInt(3));
                    summary.put("Pending_Count", cs.getInt(4));
                    return summary;
                });
    }
}
