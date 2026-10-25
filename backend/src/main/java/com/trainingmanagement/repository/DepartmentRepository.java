package com.trainingmanagement.repository;

import com.trainingmanagement.model.Department;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Data access for the department table using JdbcTemplate.
 *
 * Every method sends plain SQL to MySQL through JDBC; "?" placeholders are
 * filled by a PreparedStatement, which also protects against SQL injection.
 */
@Repository
public class DepartmentRepository {

    /**
     * LEFT JOIN + GROUP BY + aggregate functions:
     * every department with its number of students and their average CGPA.
     */
    private static final String SELECT_WITH_STATS = """
            SELECT d.Dept_ID,
                   d.Dept_Name,
                   d.HOD_Name,
                   COUNT(s.Student_ID)  AS Student_Count,
                   ROUND(AVG(s.CGPA), 2) AS Avg_CGPA
              FROM department d
              LEFT JOIN student s ON s.Dept_ID = d.Dept_ID
            """;

    private static final String GROUP_BY = " GROUP BY d.Dept_ID, d.Dept_Name, d.HOD_Name ";

    /** Converts one row of the ResultSet into a Department object. */
    private static final RowMapper<Department> ROW_MAPPER = (rs, rowNum) -> {
        Department d = new Department();
        d.setDeptId(rs.getInt("Dept_ID"));
        d.setDeptName(rs.getString("Dept_Name"));
        d.setHodName(rs.getString("HOD_Name"));
        d.setStudentCount(rs.getInt("Student_Count"));
        d.setAvgCgpa(rs.getBigDecimal("Avg_CGPA"));
        return d;
    };

    private final JdbcTemplate jdbcTemplate;

    public DepartmentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Department> findAll() {
        return jdbcTemplate.query(SELECT_WITH_STATS + GROUP_BY + " ORDER BY d.Dept_ID", ROW_MAPPER);
    }

    public Optional<Department> findById(int deptId) {
        List<Department> rows = jdbcTemplate.query(
                SELECT_WITH_STATS + " WHERE d.Dept_ID = ? " + GROUP_BY, ROW_MAPPER, deptId);
        return rows.stream().findFirst();
    }

    public boolean existsById(int deptId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM department WHERE Dept_ID = ?", Integer.class, deptId);
        return count != null && count > 0;
    }

    /** Dept_ID is not AUTO_INCREMENT in this schema, so it is inserted explicitly. */
    public void insert(Department d) {
        jdbcTemplate.update(
                "INSERT INTO department (Dept_ID, Dept_Name, HOD_Name) VALUES (?, ?, ?)",
                d.getDeptId(), d.getDeptName(), d.getHodName());
    }

    public int update(int deptId, Department d) {
        return jdbcTemplate.update(
                "UPDATE department SET Dept_Name = ?, HOD_Name = ? WHERE Dept_ID = ?",
                d.getDeptName(), d.getHodName(), deptId);
    }

    public int delete(int deptId) {
        return jdbcTemplate.update("DELETE FROM department WHERE Dept_ID = ?", deptId);
    }

    public int countStudents(int deptId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM student WHERE Dept_ID = ?", Integer.class, deptId);
        return count == null ? 0 : count;
    }
}
