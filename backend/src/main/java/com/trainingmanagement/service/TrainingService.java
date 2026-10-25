package com.trainingmanagement.service;

import com.trainingmanagement.exception.ConflictException;
import com.trainingmanagement.exception.ResourceNotFoundException;
import com.trainingmanagement.model.Training;
import com.trainingmanagement.repository.TrainingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/** Business rules for training programmes. */
@Service
public class TrainingService {

    private final TrainingRepository trainingRepository;

    public TrainingService(TrainingRepository trainingRepository) {
        this.trainingRepository = trainingRepository;
    }

    public List<Training> getAll(String search) {
        return trainingRepository.findAll(search);
    }

    public Training getById(int id) {
        return trainingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Training with ID " + id + " not found."));
    }

    public Training create(Training input) {
        validate(input);
        int newId = trainingRepository.insert(input);
        return getById(newId);
    }

    public Training update(int id, Training input) {
        if (!trainingRepository.existsById(id)) {
            throw new ResourceNotFoundException("Training with ID " + id + " not found.");
        }
        validate(input);
        trainingRepository.update(id, input);
        return getById(id);
    }

    public void delete(int id) {
        if (!trainingRepository.existsById(id)) {
            throw new ResourceNotFoundException("Training with ID " + id + " not found.");
        }
        int sessions = trainingRepository.countSessions(id);
        if (sessions > 0) {
            throw new ConflictException("Cannot delete this training: it has " + sessions
                    + " session(s). Delete those sessions first.");
        }
        trainingRepository.delete(id);
    }

    private void validate(Training t) {
        t.setTrName(InputValidator.requireText(t.getTrName(), "Tr_Name", 50));
        t.setTrType(InputValidator.optionalText(t.getTrType(), "Tr_Type", 30));
        t.setDescription(InputValidator.optionalText(t.getDescription(), "Description", 100));
        InputValidator.optionalRange(t.getDurationHrs(), "Duration_Hrs", 1, 1000);
    }
}
