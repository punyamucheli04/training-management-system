package com.trainingmanagement.service;

import com.trainingmanagement.exception.BadRequestException;
import com.trainingmanagement.exception.ResourceNotFoundException;
import com.trainingmanagement.model.TrainingSession;
import com.trainingmanagement.repository.EnrollmentRepository;
import com.trainingmanagement.repository.SessionRepository;
import com.trainingmanagement.repository.TrainerRepository;
import com.trainingmanagement.repository.TrainingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Business rules for training sessions.
 * The "trainer double-booking" rule is enforced by the MySQL trigger
 * trg_session_before_insert / trg_session_before_update.
 */
@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final TrainingRepository trainingRepository;
    private final TrainerRepository trainerRepository;
    private final EnrollmentRepository enrollmentRepository;

    public SessionService(SessionRepository sessionRepository,
                          TrainingRepository trainingRepository,
                          TrainerRepository trainerRepository,
                          EnrollmentRepository enrollmentRepository) {
        this.sessionRepository = sessionRepository;
        this.trainingRepository = trainingRepository;
        this.trainerRepository = trainerRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    public List<TrainingSession> getAll(String status, String search) {
        return sessionRepository.findAll(status, search);
    }

    public TrainingSession getById(int id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session with ID " + id + " not found."));
    }

    public TrainingSession create(TrainingSession input) {
        validate(input);
        int newId = sessionRepository.insert(input);
        return getById(newId);
    }

    public TrainingSession update(int id, TrainingSession input) {
        if (!sessionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Session with ID " + id + " not found.");
        }
        validate(input);
        sessionRepository.update(id, input);
        return getById(id);
    }

    /** Deletes the session's enrollments and the session in one transaction. */
    @Transactional
    public void delete(int id) {
        if (!sessionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Session with ID " + id + " not found.");
        }
        enrollmentRepository.deleteBySession(id);
        sessionRepository.delete(id);
    }

    /** Uses the stored procedure GetSessionAttendanceSummary (OUT parameters). */
    public Map<String, Object> attendanceSummary(int id) {
        if (!sessionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Session with ID " + id + " not found.");
        }
        return sessionRepository.attendanceSummary(id);
    }

    private void validate(TrainingSession s) {
        InputValidator.require(s.getSessionDate(), "Session_Date");
        InputValidator.require(s.getStartTime(), "Start_Time");
        InputValidator.require(s.getEndTime(), "End_Time");
        InputValidator.require(s.getTrainingId(), "Training_ID");
        InputValidator.require(s.getTrainerId(), "Trainer_ID");

        if (!s.getEndTime().isAfter(s.getStartTime())) {
            throw new BadRequestException("End_Time must be later than Start_Time.");
        }
        if (!trainingRepository.existsById(s.getTrainingId())) {
            throw new BadRequestException("Training with ID " + s.getTrainingId() + " does not exist.");
        }
        if (!trainerRepository.existsById(s.getTrainerId())) {
            throw new BadRequestException("Trainer with ID " + s.getTrainerId() + " does not exist.");
        }
    }
}
