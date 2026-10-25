package com.trainingmanagement.controller;

import com.trainingmanagement.service.EnrollmentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** Read-only attendance reports. */
@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final EnrollmentService enrollmentService;

    public AttendanceController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    /** Attendance percentage of every student (GROUP BY + SUM(CASE ...)). */
    @GetMapping("/students")
    public List<Map<String, Object>> byStudent() {
        return enrollmentService.attendanceByStudent();
    }

    /** Latest rows of attendance_audit (filled by a trigger). */
    @GetMapping("/history")
    public List<Map<String, Object>> history() {
        return enrollmentService.recentAttendanceChanges();
    }
}
