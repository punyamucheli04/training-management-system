package com.trainingmanagement.repository;

import com.trainingmanagement.exception.ConflictException;
import com.trainingmanagement.exception.ResourceNotFoundException;
import com.trainingmanagement.model.Enrollment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access for the session_enrollment table.
 *
 * enrollStudent() is written with PLAIN JDBC (Connection, PreparedStatement,
 * commit, rollback) to show exactly how a database transaction works.
 * The other methods use JdbcTemplate.
 */
@Repository
public class EnrollmentRepository {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentRepository.class);

    private static final String SELECT_ENROLLMENTS = """
            SELECT Session_ID, Student_ID, Student_Name, Student_Email, Dept_Name,
                   Session_Date, Start_Time, End_Time, Training_ID, Tr_Name, Trainer_Name,
                   Attendance_Status, Enrolled_At
              FROM enrollment_details_view
            """;

    private static final RowMapper<Enrollment> ROW_MAPPER = (rs, rowNum) -> {
        Enrollment e = new Enrollment();
        e.setSessionId(rs.getInt("Session_ID"));
        e.setStudentId(rs.getInt("Student_ID"));
        e.setStudentName(rs.getString("Student_Name"));
        e.setStudentEmail(rs.getString("Student_Email"));
        e.setDeptName(rs.getString("Dept_Name"));
        e.setSessionDate(RowUtils.getDate(rs, "Session_Date"));
        e.setStartTime(RowUtils.getTime(rs, "Start_Time"));
        e.setEndTime(RowUtils.getTime(rs, "End_Time"));
        e.setTrainingId(rs.getInt("Training_ID"));
        e.setTrName(rs.getString("Tr_Name"));
        e.setTrainerName(rs.getString("Trainer_Name"));
        e.setAttendanceStatus(rs.getString("Attendance_Status"));
        e.setEnrolledAt(RowUtils.getDateTime(rs, "Enrolled_At"));
        return e;
    };

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public EnrollmentRepository(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    // ------------------------------------------------------------------ reads

    public List<Enrollment> findAll(Integer sessionId, Integer studentId) {
        StringBuilder sql = new StringBuilder(SELECT_ENROLLMENTS).append(" WHERE 1 = 1");
        List<Object> params = new ArrayList<>();
        if (sessionId != null) {
            sql.append(" AND Session_ID = ?");
            params.add(sessionId);
        }
        if (studentId != null) {
            sql.append(" AND Student_ID = ?");
            params.add(studentId);
        }
        sql.append(sessionId != null ? " ORDER BY Student_Name" : " ORDER BY Enrolled_At DESC, Session_ID DESC");
        return jdbcTemplate.query(sql.toString(), ROW_MAPPER, params.toArray());
    }

    public List<Enrollment> findRecent(int limit) {
        return jdbcTemplate.query(SELECT_ENROLLMENTS + " ORDER BY Enrolled_At DESC, Session_ID DESC LIMIT ?",
                ROW_MAPPER, limit);
    }

    public boolean exists(int sessionId, int studentId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM session_enrollment WHERE Session_ID = ? AND Student_ID = ?",
                Integer.class, sessionId, studentId);
        return count != null && count > 0;
    }

    // ------------------------------------------------------------------ JDBC transaction

    /**
     * Enrolls a student in a session inside ONE database transaction.
     *
     *   1. setAutoCommit(false)            -> transaction starts
     *   2. check the student exists
     *   3. check the session exists
     *   4. check the student is not already enrolled
     *   5. check the student has no other session at an overlapping time
     *   6. INSERT the enrollment
     *   7. INSERT a history row into attendance_audit
     *   8. commit()                         -> both inserts become permanent
     *
     * If any step fails, rollback() undoes everything done in this transaction,
     * so the database never contains an enrollment without its history row.
     */
    public void enrollStudent(int sessionId, int studentId, String attendanceStatus) {
        try (Connection con = dataSource.getConnection()) {
            con.setAutoCommit(false); // BEGIN TRANSACTION
            try {
                // Step 2: student must exist
                if (!rowExists(con, "SELECT 1 FROM student WHERE Student_ID = ?", studentId)) {
                    throw new ResourceNotFoundException("Student with ID " + studentId + " does not exist.");
                }

                // Step 3: session must exist (read its date and time for step 5)
                LocalDate sessionDate;
                LocalTime startTime;
                LocalTime endTime;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT Session_Date, Start_Time, End_Time FROM training_session WHERE Session_ID = ?")) {
                    ps.setInt(1, sessionId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new ResourceNotFoundException("Session with ID " + sessionId + " does not exist.");
                        }
                        sessionDate = rs.getObject("Session_Date", LocalDate.class);
                        startTime = rs.getObject("Start_Time", LocalTime.class);
                        endTime = rs.getObject("End_Time", LocalTime.class);
                    }
                }

                // Step 4: no duplicate enrollment (the composite PRIMARY KEY is the final guarantee)
                if (rowExists(con, "SELECT 1 FROM session_enrollment WHERE Session_ID = ? AND Student_ID = ?",
                        sessionId, studentId)) {
                    throw new ConflictException("This student is already enrolled in session " + sessionId + ".");
                }

                // Step 5: the student must be free at that time (JOIN + overlap condition)
                try (PreparedStatement ps = con.prepareStatement("""
                        SELECT ts.Session_ID
                          FROM session_enrollment e
                          JOIN training_session ts ON e.Session_ID = ts.Session_ID
                         WHERE e.Student_ID = ?
                           AND ts.Session_Date = ?
                           AND ts.Start_Time < ?
                           AND ts.End_Time > ?
                         LIMIT 1
                        """)) {
                    ps.setInt(1, studentId);
                    ps.setObject(2, sessionDate);
                    ps.setObject(3, endTime);
                    ps.setObject(4, startTime);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            throw new ConflictException("The student is already enrolled in session "
                                    + rs.getInt(1) + " at an overlapping time on " + sessionDate + ".");
                        }
                    }
                }

                // Step 6: insert the enrollment (trigger trg_enrollment_before_insert validates the status)
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO session_enrollment (Session_ID, Student_ID, Attendance_Status) VALUES (?, ?, ?)")) {
                    ps.setInt(1, sessionId);
                    ps.setInt(2, studentId);
                    ps.setString(3, attendanceStatus);
                    ps.executeUpdate();
                }

                // Step 7: record the enrollment in the history table
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO attendance_audit (Session_ID, Student_ID, Old_Status, New_Status, Changed_By) "
                                + "VALUES (?, ?, NULL, ?, CURRENT_USER())")) {
                    ps.setInt(1, sessionId);
                    ps.setInt(2, studentId);
                    ps.setString(3, attendanceStatus);
                    ps.executeUpdate();
                }

                con.commit(); // Step 8: COMMIT
                log.info("Transaction committed: student {} enrolled in session {}", studentId, sessionId);

            } catch (RuntimeException | SQLException ex) {
                con.rollback(); // ROLLBACK - nothing from this transaction is kept
                log.info("Transaction rolled back: {}", ex.getMessage());
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            // Convert the checked SQLException into Spring's DataAccessException
            // so GlobalExceptionHandler can turn it into a friendly message.
            DataAccessException translated = jdbcTemplate.getExceptionTranslator()
                    .translate("Enroll student", null, ex);
            throw translated != null ? translated : new UncategorizedSQLException("Enroll student", null, ex);
        }
    }

    private boolean rowExists(Connection con, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // ------------------------------------------------------------------ writes (JdbcTemplate)

    public int updateAttendance(int sessionId, int studentId, String status) {
        // trg_enrollment_before_update validates the value and the session date,
        // trg_enrollment_after_update writes the change to attendance_audit.
        return jdbcTemplate.update(
                "UPDATE session_enrollment SET Attendance_Status = ? WHERE Session_ID = ? AND Student_ID = ?",
                status, sessionId, studentId);
    }

    public int delete(int sessionId, int studentId) {
        return jdbcTemplate.update(
                "DELETE FROM session_enrollment WHERE Session_ID = ? AND Student_ID = ?", sessionId, studentId);
    }

    public int deleteBySession(int sessionId) {
        return jdbcTemplate.update("DELETE FROM session_enrollment WHERE Session_ID = ?", sessionId);
    }

    public int deleteByStudent(int studentId) {
        return jdbcTemplate.update("DELETE FROM session_enrollment WHERE Student_ID = ?", studentId);
    }
}
