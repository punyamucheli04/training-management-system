package com.trainingmanagement.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Checks the JDBC connection and looks in MySQL's data dictionary
 * (information_schema) for the objects created by the database scripts.
 */
@Repository
public class HealthRepository {

    /** object name -> script that creates it */
    private static final Map<String, String> REQUIRED_VIEWS = Map.of(
            "student_department_view", "views.sql",
            "session_details_view", "views.sql",
            "enrollment_details_view", "views.sql");

    private static final Map<String, String> REQUIRED_ROUTINES = Map.of(
            "calculate_attendance_percentage", "functions.sql",
            "session_duration_hours", "functions.sql",
            "GetStudentAttendance", "procedures.sql",
            "GetTrainingEnrollmentCount", "procedures.sql",
            "GetSessionAttendanceSummary", "procedures.sql");

    private static final Map<String, String> REQUIRED_TRIGGERS = Map.of(
            "trg_enrollment_before_insert", "triggers.sql",
            "trg_enrollment_before_update", "triggers.sql",
            "trg_enrollment_after_update", "triggers.sql",
            "trg_session_before_insert", "triggers.sql",
            "trg_session_before_update", "triggers.sql");

    private final JdbcTemplate jdbcTemplate;

    public HealthRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> connectionInfo() {
        return jdbcTemplate.queryForObject(
                "SELECT DATABASE() AS db_name, VERSION() AS db_version",
                (rs, rowNum) -> {
                    Map<String, Object> info = new LinkedHashMap<>();
                    info.put("database", rs.getString("db_name"));
                    info.put("version", rs.getString("db_version"));
                    return info;
                });
    }

    /** Returns entries like "session_details_view (views.sql)" for every missing object. */
    public List<String> missingDatabaseObjects() {
        List<String> views = jdbcTemplate.queryForList(
                "SELECT TABLE_NAME FROM information_schema.VIEWS WHERE TABLE_SCHEMA = DATABASE()",
                String.class);
        List<String> routines = jdbcTemplate.queryForList(
                "SELECT ROUTINE_NAME FROM information_schema.ROUTINES WHERE ROUTINE_SCHEMA = DATABASE()",
                String.class);
        List<String> triggers = jdbcTemplate.queryForList(
                "SELECT TRIGGER_NAME FROM information_schema.TRIGGERS WHERE TRIGGER_SCHEMA = DATABASE()",
                String.class);
        Integer auditTable = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.TABLES "
                        + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'attendance_audit'",
                Integer.class);

        List<String> missing = new ArrayList<>();
        addMissing(missing, REQUIRED_VIEWS, views);
        addMissing(missing, REQUIRED_ROUTINES, routines);
        addMissing(missing, REQUIRED_TRIGGERS, triggers);
        if (auditTable == null || auditTable == 0) {
            missing.add("attendance_audit (triggers.sql)");
        }
        missing.sort(String::compareTo);
        return missing;
    }

    private void addMissing(List<String> missing, Map<String, String> required, List<String> existing) {
        List<String> existingLower = existing.stream().map(String::toLowerCase).toList();
        required.forEach((name, script) -> {
            if (!existingLower.contains(name.toLowerCase())) {
                missing.add(name + " (" + script + ")");
            }
        });
    }
}
