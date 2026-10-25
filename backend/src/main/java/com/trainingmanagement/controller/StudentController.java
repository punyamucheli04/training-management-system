package com.trainingmanagement.controller;

import com.trainingmanagement.model.Student;
import com.trainingmanagement.service.StudentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** REST endpoints for students. Controllers contain no SQL - they call the service layer. */
@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    /** GET /api/students?search=rah */
    @GetMapping
    public List<Student> getAll(@RequestParam(name = "search", required = false) String search) {
        return studentService.getAll(search);
    }

    @GetMapping("/{id}")
    public Student getById(@PathVariable("id") int id) {
        return studentService.getById(id);
    }

    /** Attendance history of one student (stored procedure GetStudentAttendance). */
    @GetMapping("/{id}/attendance")
    public Map<String, Object> getAttendance(@PathVariable("id") int id) {
        return studentService.attendance(id);
    }

    @PostMapping
    public ResponseEntity<Student> create(@RequestBody Student student) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.create(student));
    }

    @PutMapping("/{id}")
    public Student update(@PathVariable("id") int id, @RequestBody Student student) {
        return studentService.update(id, student);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") int id) {
        studentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
