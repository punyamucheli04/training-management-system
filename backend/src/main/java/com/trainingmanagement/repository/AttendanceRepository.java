package com.trainingmanagement.repository;

import com.trainingmanagement.model.AttendanceRecord;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Attendance queries: stored procedure, percentages and the audit log. */
@Repository
public class AttendanceRepository {

    private final JdbcTemplate jdbcTemplate;

    public AttendanceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Calls the STORED PROCEDURE GetStudentAttendance(student_id) through a
     * JDBC CallableStatement and reads the result set it returns.
     */
    public List<AttendanceRecord> studentAttendance(int studentId) {
        return jdbcTemplate.execute(
                connection -> connection.prepareCall("{call GetStudentAttendance(?)}"),
                (CallableStatementCallback<List<AttendanceRecord>>) cs -> {
                    cs.setInt(1, studentId);
                    List<AttendanceRecord> records = new ArrayList<>();
                    boolean hasResultSet = cs.execute();
                    if (hasResultSet) {
                        try (ResultSet rs = cs.getResultSet()) {
                            while (rs.next()) {
                                AttendanceRecord r = new AttendanceRecord();
                                r.setSessionId(rs.getInt("Session_ID"));
                                r.setSessionDate(RowUtils.getDate(rs, "Session_Date"));
                                r.setStartTime(RowUtils.getTime(rs, "Start_Time"));
                                r.setEndTime(RowUtils.getTime(rs, "End_Time"));
                                r.setTrName(rs.getString("Tr_Name"));
                                r.setTrainerName(rs.getString("Trainer_Name"));
                                r.setAttendanceStatus(rs.getString("Attendance_Status"));
                                records.add(r);
                            }
                        }
                    }
                    return records;
                });
    }

    /** Calls the STORED FUNCTION calculate_attendance_percentage() for one student. */
    public java.math.BigDecimal attendancePercentage(int studentId) {
        return jdbcTemplate.queryForObject(
                "SELECT calculate_attendance_percentage(?)", java.math.BigDecimal.class, studentId);
    }

    /**
     * Attendance percentage of every student, calculated in SQL with
     * LEFT JOIN + GROUP BY + SUM(CASE ...) + COUNT.
     */
    public List<Map<String, Object>> attendanceByStudent() {
        return jdbcTemplate.queryForList("""
                SELECT s.Student_ID,
                       s.Name,
                       d.Dept_Name,
                       COUNT(e.Session_ID)                                         AS Enrolled_Sessions,
                       SUM(CASE WHEN e.Attendance_Status = 'Present' THEN 1 ELSE 0 END) AS Present_Count,
                       SUM(CASE WHEN e.Attendance_Status = 'Absent'  THEN 1 ELSE 0 END) AS Absent_Count,
                       SUM(CASE WHEN e.Attendance_Status = 'Pending' THEN 1 ELSE 0 END) AS Pending_Count,
                       ROUND(100 * SUM(CASE WHEN e.Attendance_Status = 'Present' THEN 1 ELSE 0 END)
                             / NULLIF(SUM(CASE WHEN e.Attendance_Status IN ('Present', 'Absent') THEN 1 ELSE 0 END), 0), 2)
                                                                                   AS Attendance_Pct
                  FROM student s
                  LEFT JOIN department d          ON s.Dept_ID = d.Dept_ID
                  LEFT JOIN session_enrollment e  ON e.Student_ID = s.Student_ID
                 GROUP BY s.Student_ID, s.Name, d.Dept_Name
                 ORDER BY Attendance_Pct DESC, s.Name
                """);
    }

    /** Latest rows written by trigger trg_enrollment_after_update (and by enrollments). */
    public List<Map<String, Object>> recentChanges(int limit) {
        return jdbcTemplate.queryForList("""
                SELECT a.Audit_ID,
                       a.Session_ID,
                       a.Student_ID,
                       s.Name   AS Student_Name,
                       t.Tr_Name,
                       a.Old_Status,
                       a.New_Status,
                       a.Changed_By,
                       DATE_FORMAT(a.Changed_At, '%Y-%m-%d %H:%i:%s') AS Changed_At
                  FROM attendance_audit a
                  LEFT JOIN student          s  ON a.Student_ID  = s.Student_ID
                  LEFT JOIN training_session ts ON a.Session_ID  = ts.Session_ID
                  LEFT JOIN training         t  ON ts.Training_ID = t.Training_ID
                 ORDER BY a.Changed_At DESC, a.Audit_ID DESC
                 LIMIT ?
                """, limit);
    }
}
