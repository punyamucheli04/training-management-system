package com.trainingmanagement.repository;

import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SQL reports that demonstrate DBMS concepts: JOIN, GROUP BY, HAVING,
 * aggregate functions, subqueries (scalar, correlated, EXISTS / NOT EXISTS,
 * NOT IN), stored functions and a stored procedure.
 *
 * The SQL text is also sent to the frontend so it can be shown on the
 * Reports page during a demonstration.
 */
@Repository
public class ReportRepository {

    /** Small description of one report. */
    public record ReportDefinition(String key, String title, String technique, String description, String sql) {
    }

    public static final String PROCEDURE_REPORT = "training-enrollment";

    private static final Map<String, ReportDefinition> REPORTS = new LinkedHashMap<>();

    static {
        add("department-strength", "Students per department",
                "LEFT JOIN, GROUP BY, COUNT, AVG, MAX, MIN",
                "Number of students in every department and their CGPA statistics.",
                """
                SELECT d.Dept_ID,
                       d.Dept_Name,
                       COUNT(s.Student_ID)   AS Students,
                       ROUND(AVG(s.CGPA), 2) AS Avg_CGPA,
                       MAX(s.CGPA)           AS Max_CGPA,
                       MIN(s.CGPA)           AS Min_CGPA
                  FROM department d
                  LEFT JOIN student s ON s.Dept_ID = d.Dept_ID
                 GROUP BY d.Dept_ID, d.Dept_Name
                 ORDER BY Students DESC, d.Dept_ID
                """);

        add("popular-sessions", "Sessions with more than one student",
                "JOIN, GROUP BY, HAVING",
                "Only groups (sessions) whose enrollment count is greater than 1 are kept by HAVING.",
                """
                SELECT e.Session_ID,
                       t.Tr_Name,
                       DATE_FORMAT(ts.Session_Date, '%Y-%m-%d') AS Session_Date,
                       COUNT(*) AS Enrolled_Students
                  FROM session_enrollment e
                  JOIN training_session ts ON e.Session_ID   = ts.Session_ID
                  JOIN training         t  ON ts.Training_ID = t.Training_ID
                 GROUP BY e.Session_ID, t.Tr_Name, ts.Session_Date
                HAVING COUNT(*) > 1
                 ORDER BY Enrolled_Students DESC, e.Session_ID
                """);

        add("low-attendance", "Students below 75% attendance",
                "Stored function in WHERE, EXISTS subquery",
                "Uses calculate_attendance_percentage() for every student who has at least one marked session.",
                """
                SELECT s.Student_ID,
                       s.Name,
                       d.Dept_Name,
                       calculate_attendance_percentage(s.Student_ID) AS Attendance_Pct
                  FROM student s
                  LEFT JOIN department d ON s.Dept_ID = d.Dept_ID
                 WHERE EXISTS (SELECT 1
                                 FROM session_enrollment e
                                WHERE e.Student_ID = s.Student_ID
                                  AND e.Attendance_Status IN ('Present', 'Absent'))
                   AND calculate_attendance_percentage(s.Student_ID) < 75
                 ORDER BY Attendance_Pct
                """);

        add("above-average-cgpa", "Students above the college average CGPA",
                "Scalar subquery, AVG",
                "The inner SELECT computes the average CGPA once; the outer query keeps students above it.",
                """
                SELECT s.Student_ID,
                       s.Name,
                       d.Dept_Name,
                       s.CGPA,
                       (SELECT ROUND(AVG(CGPA), 2) FROM student) AS College_Avg_CGPA
                  FROM student s
                  LEFT JOIN department d ON s.Dept_ID = d.Dept_ID
                 WHERE s.CGPA > (SELECT AVG(CGPA) FROM student)
                 ORDER BY s.CGPA DESC
                """);

        add("department-toppers", "Department toppers",
                "Correlated subquery, MAX",
                "For each student the inner query finds the highest CGPA of that student's own department.",
                """
                SELECT d.Dept_Name,
                       s.Student_ID,
                       s.Name,
                       s.CGPA
                  FROM student s
                  JOIN department d ON s.Dept_ID = d.Dept_ID
                 WHERE s.CGPA = (SELECT MAX(s2.CGPA)
                                   FROM student s2
                                  WHERE s2.Dept_ID = s.Dept_ID)
                 ORDER BY d.Dept_Name
                """);

        add("not-enrolled", "Students not enrolled in any session",
                "NOT EXISTS subquery",
                "Students for whom no matching row exists in session_enrollment.",
                """
                SELECT s.Student_ID,
                       s.Name,
                       s.Email,
                       d.Dept_Name
                  FROM student s
                  LEFT JOIN department d ON s.Dept_ID = d.Dept_ID
                 WHERE NOT EXISTS (SELECT 1
                                     FROM session_enrollment e
                                    WHERE e.Student_ID = s.Student_ID)
                 ORDER BY s.Student_ID
                """);

        add("trainer-workload", "Trainer workload",
                "LEFT JOIN, GROUP BY, COUNT, SUM, stored function, correlated subquery",
                "Sessions and teaching hours per trainer; hours use the session_duration_hours() function.",
                """
                SELECT tr.Trainer_ID,
                       tr.Trainer_Name,
                       tr.Expertise,
                       COUNT(ts.Session_ID) AS Sessions,
                       COALESCE(SUM(session_duration_hours(ts.Start_Time, ts.End_Time)), 0) AS Total_Hours,
                       (SELECT COUNT(*)
                          FROM session_enrollment e
                          JOIN training_session x ON e.Session_ID = x.Session_ID
                         WHERE x.Trainer_ID = tr.Trainer_ID) AS Students_Taught
                  FROM trainer tr
                  LEFT JOIN training_session ts ON ts.Trainer_ID = tr.Trainer_ID
                 GROUP BY tr.Trainer_ID, tr.Trainer_Name, tr.Expertise
                 ORDER BY Total_Hours DESC, tr.Trainer_ID
                """);

        add("unscheduled-trainings", "Trainings with no sessions scheduled",
                "NOT IN subquery",
                "Training programmes whose ID does not appear in training_session.",
                """
                SELECT t.Training_ID,
                       t.Tr_Name,
                       t.Tr_Type,
                       t.Duration_Hrs
                  FROM training t
                 WHERE t.Training_ID NOT IN (SELECT ts.Training_ID FROM training_session ts)
                 ORDER BY t.Training_ID
                """);

        add(PROCEDURE_REPORT, "Enrollment and attendance per training",
                "Stored procedure (CallableStatement)",
                "Result of CALL GetTrainingEnrollmentCount(), which uses LEFT JOIN, GROUP BY, COUNT and SUM.",
                "CALL GetTrainingEnrollmentCount()");
    }

    private static void add(String key, String title, String technique, String description, String sql) {
        REPORTS.put(key, new ReportDefinition(key, title, technique, description, sql.strip()));
    }

    private final JdbcTemplate jdbcTemplate;

    public ReportRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ReportDefinition> definitions() {
        return new ArrayList<>(REPORTS.values());
    }

    public ReportDefinition definition(String key) {
        return REPORTS.get(key);
    }

    /** Runs a report; each row becomes a map of column label -> value. */
    public List<Map<String, Object>> run(ReportDefinition report) {
        if (PROCEDURE_REPORT.equals(report.key())) {
            return trainingEnrollmentCount();
        }
        return jdbcTemplate.queryForList(report.sql());
    }

    /**
     * Calls the STORED PROCEDURE GetTrainingEnrollmentCount() with a JDBC
     * CallableStatement and converts its result set to a list of maps.
     */
    public List<Map<String, Object>> trainingEnrollmentCount() {
        return jdbcTemplate.execute(
                connection -> connection.prepareCall("{call GetTrainingEnrollmentCount()}"),
                (CallableStatementCallback<List<Map<String, Object>>>) cs -> {
                    List<Map<String, Object>> rows = new ArrayList<>();
                    if (cs.execute()) {
                        ColumnMapRowMapper mapper = new ColumnMapRowMapper();
                        try (ResultSet rs = cs.getResultSet()) {
                            int rowNum = 0;
                            while (rs.next()) {
                                rows.add(mapper.mapRow(rs, rowNum++));
                            }
                        }
                    }
                    return rows;
                });
    }
}
