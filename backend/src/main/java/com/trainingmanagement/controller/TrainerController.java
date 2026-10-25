package com.trainingmanagement.controller;

import com.trainingmanagement.model.Trainer;
import com.trainingmanagement.service.TrainerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** REST endpoints for trainers. */
@RestController
@RequestMapping("/api/trainers")
public class TrainerController {

    private final TrainerService trainerService;

    public TrainerController(TrainerService trainerService) {
        this.trainerService = trainerService;
    }

    @GetMapping
    public List<Trainer> getAll(@RequestParam(name = "search", required = false) String search) {
        return trainerService.getAll(search);
    }

    @GetMapping("/{id}")
    public Trainer getById(@PathVariable("id") int id) {
        return trainerService.getById(id);
    }

    @PostMapping
    public ResponseEntity<Trainer> create(@RequestBody Trainer trainer) {
        return ResponseEntity.status(HttpStatus.CREATED).body(trainerService.create(trainer));
    }

    @PutMapping("/{id}")
    public Trainer update(@PathVariable("id") int id, @RequestBody Trainer trainer) {
        return trainerService.update(id, trainer);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") int id) {
        trainerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
