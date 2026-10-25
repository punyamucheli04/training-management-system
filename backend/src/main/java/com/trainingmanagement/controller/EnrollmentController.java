package com.trainingmanagement.controller;

import com.trainingmanagement.model.AttendanceRequest;
import com.trainingmanagement.model.Enrollment;
import com.trainingmanagement.model.EnrollmentRequest;
import com.trainingmanagement.service.EnrollmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** REST endpoints for enrollments and attendance. */
@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    /** GET /api/enrollments?sessionId=401&studentId=1 (both optional) */
    @GetMapping
    public List<Enrollment> getAll(@RequestParam(name = "sessionId", required = false) Integer sessionId,
                                   @RequestParam(name = "studentId", required = false) Integer studentId) {
        return enrollmentService.getAll(sessionId, studentId);
    }

    /** Enroll a student - runs as a JDBC transaction (commit / rollback). */
    @PostMapping
    public ResponseEntity<Enrollment> enroll(@RequestBody EnrollmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollmentService.enroll(request));
    }

    @DeleteMapping("/{sessionId}/{studentId}")
    public ResponseEntity<Void> unenroll(@PathVariable("sessionId") int sessionId, @PathVariable("studentId") int studentId) {
        enrollmentService.unenroll(sessionId, studentId);
        return ResponseEntity.noContent().build();
    }

    /** Body: { "Attendance_Status": "Present" }  (Present, Absent or Pending) */
    @PutMapping("/{sessionId}/{studentId}/attendance")
    public Enrollment updateAttendance(@PathVariable("sessionId") int sessionId, @PathVariable("studentId") int studentId,
                                       @RequestBody AttendanceRequest request) {
        return enrollmentService.updateAttendance(sessionId, studentId, request.getAttendanceStatus());
    }
}
