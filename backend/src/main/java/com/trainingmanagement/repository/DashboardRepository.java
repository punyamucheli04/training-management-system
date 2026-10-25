package com.trainingmanagement.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/** Numbers shown on the dashboard - all calculated by MySQL. */
@Repository
public class DashboardRepository {

    private final JdbcTemplate jdbcTemplate;

    public DashboardRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** One row built from independent scalar subqueries (one per counter). */
    public Map<String, Object> totals() {
        return jdbcTemplate.queryForMap("""
                SELECT
                    (SELECT COUNT(*) FROM student)            AS Total_Students,
                    (SELECT COUNT(*) FROM trainer)            AS Total_Trainers,
                    (SELECT COUNT(*) FROM training)           AS Total_Trainings,
                    (SELECT COUNT(*) FROM department)         AS Total_Departments,
                    (SELECT COUNT(*) FROM training_session)   AS Total_Sessions,
                    (SELECT COUNT(*) FROM training_session
                      WHERE Session_Date >= CURDATE())        AS Upcoming_Sessions,
                    (SELECT COUNT(*) FROM session_enrollment) AS Total_Enrollments,
                    (SELECT COUNT(*) FROM session_enrollment
                      WHERE Attendance_Status = 'Present')    AS Present_Count,
                    (SELECT COUNT(*) FROM session_enrollment
                      WHERE Attendance_Status = 'Absent')     AS Absent_Count,
                    (SELECT COUNT(*) FROM session_enrollment
                      WHERE Attendance_Status = 'Pending')    AS Pending_Count,
                    (SELECT ROUND(100 * SUM(Attendance_Status = 'Present')
                                 / NULLIF(SUM(Attendance_Status IN ('Present', 'Absent')), 0), 2)
                       FROM session_enrollment)               AS Overall_Attendance_Pct
                """);
    }

    /** Attendance percentage per department: 3-table JOIN + GROUP BY. */
    public List<Map<String, Object>> attendanceByDepartment() {
        return jdbcTemplate.queryForList("""
                SELECT d.Dept_ID,
                       d.Dept_Name,
                       COUNT(e.Student_ID)                                 AS Enrollments,
                       SUM(e.Attendance_Status = 'Present')               AS Present_Count,
                       SUM(e.Attendance_Status = 'Absent')                AS Absent_Count,
                       ROUND(100 * SUM(e.Attendance_Status = 'Present')
                             / NULLIF(SUM(e.Attendance_Status IN ('Present', 'Absent')), 0), 2) AS Attendance_Pct
                  FROM department d
                  JOIN student            s ON s.Dept_ID    = d.Dept_ID
                  JOIN session_enrollment e ON e.Student_ID = s.Student_ID
                 GROUP BY d.Dept_ID, d.Dept_Name
                 ORDER BY Attendance_Pct DESC
                """);
    }
}
