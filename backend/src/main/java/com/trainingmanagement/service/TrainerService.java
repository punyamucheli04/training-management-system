package com.trainingmanagement.service;

import com.trainingmanagement.exception.ConflictException;
import com.trainingmanagement.exception.ResourceNotFoundException;
import com.trainingmanagement.model.Trainer;
import com.trainingmanagement.repository.TrainerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/** Business rules for trainers. */
@Service
public class TrainerService {

    private final TrainerRepository trainerRepository;

    public TrainerService(TrainerRepository trainerRepository) {
        this.trainerRepository = trainerRepository;
    }

    public List<Trainer> getAll(String search) {
        return trainerRepository.findAll(search);
    }

    public Trainer getById(int id) {
        return trainerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer with ID " + id + " not found."));
    }

    public Trainer create(Trainer input) {
        validate(input, null);
        int newId = trainerRepository.insert(input);
        return getById(newId);
    }

    public Trainer update(int id, Trainer input) {
        if (!trainerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Trainer with ID " + id + " not found.");
        }
        validate(input, id);
        trainerRepository.update(id, input);
        return getById(id);
    }

    public void delete(int id) {
        if (!trainerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Trainer with ID " + id + " not found.");
        }
        int sessions = trainerRepository.countSessions(id);
        if (sessions > 0) {
            throw new ConflictException("Cannot delete this trainer: they are assigned to " + sessions
                    + " session(s). Reassign or delete those sessions first.");
        }
        trainerRepository.delete(id);
    }

    private void validate(Trainer t, Integer currentId) {
        t.setTrainerName(InputValidator.requireText(t.getTrainerName(), "Trainer_Name", 50));
        t.setEmail(InputValidator.optionalEmail(t.getEmail(), "Email"));
        t.setExpertise(InputValidator.optionalText(t.getExpertise(), "Expertise", 50));
        if (t.getEmail() != null && trainerRepository.emailInUse(t.getEmail(), currentId)) {
            throw new ConflictException("Email " + t.getEmail() + " is already used by another trainer.");
        }
    }
}
