package com.trainingmanagement.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Small helpers for reading nullable columns from a JDBC ResultSet.
 *
 * rs.getInt() returns 0 for SQL NULL, so nullable INT columns are read with
 * getObject(column, Integer.class) instead. DATE / TIME / TIMESTAMP columns
 * are read directly as java.time types (supported by MySQL Connector/J).
 */
final class RowUtils {

    private RowUtils() {
    }

    static Integer getInteger(ResultSet rs, String column) throws SQLException {
        return rs.getObject(column, Integer.class);
    }

    static LocalDate getDate(ResultSet rs, String column) throws SQLException {
        return rs.getObject(column, LocalDate.class);
    }

    static LocalTime getTime(ResultSet rs, String column) throws SQLException {
        return rs.getObject(column, LocalTime.class);
    }

    static LocalDateTime getDateTime(ResultSet rs, String column) throws SQLException {
        return rs.getObject(column, LocalDateTime.class);
    }
}
