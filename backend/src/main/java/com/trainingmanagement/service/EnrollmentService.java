package com.trainingmanagement.service;

import com.trainingmanagement.exception.BadRequestException;
import com.trainingmanagement.exception.ResourceNotFoundException;
import com.trainingmanagement.model.BulkAttendanceItem;
import com.trainingmanagement.model.Enrollment;
import com.trainingmanagement.model.EnrollmentRequest;
import com.trainingmanagement.repository.AttendanceRepository;
import com.trainingmanagement.repository.EnrollmentRepository;
import com.trainingmanagement.repository.SessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/** Enrollment and attendance rules. */
@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final SessionRepository sessionRepository;
    private final AttendanceRepository attendanceRepository;

    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                             SessionRepository sessionRepository,
                             AttendanceRepository attendanceRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.sessionRepository = sessionRepository;
        this.attendanceRepository = attendanceRepository;
    }

    public List<Enrollment> getAll(Integer sessionId, Integer studentId) {
        return enrollmentRepository.findAll(sessionId, studentId);
    }

    /** Validates input, then runs the JDBC transaction in EnrollmentRepository. */
    public Enrollment enroll(EnrollmentRequest request) {
        int sessionId = InputValidator.require(request.getSessionId(), "Session_ID");
        int studentId = InputValidator.require(request.getStudentId(), "Student_ID");
        String status = request.getAttendanceStatus() == null || request.getAttendanceStatus().isBlank()
                ? "Pending"
                : InputValidator.attendanceStatus(request.getAttendanceStatus());

        enrollmentRepository.enrollStudent(sessionId, studentId, status);
        return findOne(sessionId, studentId);
    }

    public void unenroll(int sessionId, int studentId) {
        if (enrollmentRepository.delete(sessionId, studentId) == 0) {
            throw new ResourceNotFoundException(
                    "Student " + studentId + " is not enrolled in session " + sessionId + ".");
        }
    }

    public Enrollment updateAttendance(int sessionId, int studentId, String rawStatus) {
        String status = InputValidator.attendanceStatus(rawStatus);
        if (!enrollmentRepository.exists(sessionId, studentId)) {
            throw new ResourceNotFoundException(
                    "Student " + studentId + " is not enrolled in session " + sessionId + ".");
        }
        enrollmentRepository.updateAttendance(sessionId, studentId, status);
        return findOne(sessionId, studentId);
    }

    /**
     * Saves the attendance of many students of one session.
     * @Transactional: all updates succeed together, or none are saved
     * (for example if the trigger rejects a future session).
     */
    @Transactional
    public List<Enrollment> updateSessionAttendance(int sessionId, List<BulkAttendanceItem> items) {
        if (!sessionRepository.existsById(sessionId)) {
            throw new ResourceNotFoundException("Session with ID " + sessionId + " not found.");
        }
        if (items == null || items.isEmpty()) {
            throw new BadRequestException("No attendance entries were sent.");
        }
        for (BulkAttendanceItem item : items) {
            int studentId = InputValidator.require(item.getStudentId(), "Student_ID");
            String status = InputValidator.attendanceStatus(item.getAttendanceStatus());
            if (enrollmentRepository.updateAttendance(sessionId, studentId, status) == 0) {
                throw new ResourceNotFoundException(
                        "Student " + studentId + " is not enrolled in session " + sessionId + ".");
            }
        }
        return enrollmentRepository.findAll(sessionId, null);
    }

    public List<Map<String, Object>> attendanceByStudent() {
        return attendanceRepository.attendanceByStudent();
    }

    public List<Map<String, Object>> recentAttendanceChanges() {
        return attendanceRepository.recentChanges(25);
    }

    private Enrollment findOne(int sessionId, int studentId) {
        return enrollmentRepository.findAll(sessionId, studentId).stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student " + studentId + " is not enrolled in session " + sessionId + "."));
    }
}
