package com.trainingmanagement.controller;

import com.trainingmanagement.model.Training;
import com.trainingmanagement.service.TrainingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** REST endpoints for training programmes. */
@RestController
@RequestMapping("/api/trainings")
public class TrainingController {

    private final TrainingService trainingService;

    public TrainingController(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    @GetMapping
    public List<Training> getAll(@RequestParam(name = "search", required = false) String search) {
        return trainingService.getAll(search);
    }

    @GetMapping("/{id}")
    public Training getById(@PathVariable("id") int id) {
        return trainingService.getById(id);
    }

    @PostMapping
    public ResponseEntity<Training> create(@RequestBody Training training) {
        return ResponseEntity.status(HttpStatus.CREATED).body(trainingService.create(training));
    }

    @PutMapping("/{id}")
    public Training update(@PathVariable("id") int id, @RequestBody Training training) {
        return trainingService.update(id, training);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") int id) {
        trainingService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
