package com.trainingmanagement.controller;

import com.trainingmanagement.model.BulkAttendanceItem;
import com.trainingmanagement.model.Enrollment;
import com.trainingmanagement.model.TrainingSession;
import com.trainingmanagement.service.EnrollmentService;
import com.trainingmanagement.service.SessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** REST endpoints for training sessions. */
@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;
    private final EnrollmentService enrollmentService;

    public SessionController(SessionService sessionService, EnrollmentService enrollmentService) {
        this.sessionService = sessionService;
        this.enrollmentService = enrollmentService;
    }

    /** GET /api/sessions?status=upcoming|completed&search=java */
    @GetMapping
    public List<TrainingSession> getAll(@RequestParam(name = "status", required = false) String status,
                                        @RequestParam(name = "search", required = false) String search) {
        return sessionService.getAll(status, search);
    }

    @GetMapping("/{id}")
    public TrainingSession getById(@PathVariable("id") int id) {
        return sessionService.getById(id);
    }

    /** Present / absent / pending counts from stored procedure GetSessionAttendanceSummary. */
    @GetMapping("/{id}/attendance-summary")
    public Map<String, Object> attendanceSummary(@PathVariable("id") int id) {
        return sessionService.attendanceSummary(id);
    }

    /** Save the attendance of several students in one transaction. */
    @PutMapping("/{id}/attendance")
    public List<Enrollment> updateAttendance(@PathVariable("id") int id, @RequestBody List<BulkAttendanceItem> items) {
        return enrollmentService.updateSessionAttendance(id, items);
    }

    @PostMapping
    public ResponseEntity<TrainingSession> create(@RequestBody TrainingSession session) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sessionService.create(session));
    }

    @PutMapping("/{id}")
    public TrainingSession update(@PathVariable("id") int id, @RequestBody TrainingSession session) {
        return sessionService.update(id, session);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") int id) {
        sessionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
