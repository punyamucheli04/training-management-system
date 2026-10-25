package com.trainingmanagement.service;

import com.trainingmanagement.exception.BadRequestException;
import com.trainingmanagement.exception.ConflictException;
import com.trainingmanagement.exception.ResourceNotFoundException;
import com.trainingmanagement.model.AttendanceRecord;
import com.trainingmanagement.model.Student;
import com.trainingmanagement.repository.AttendanceRepository;
import com.trainingmanagement.repository.DepartmentRepository;
import com.trainingmanagement.repository.EnrollmentRepository;
import com.trainingmanagement.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Business rules for students. */
@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final DepartmentRepository departmentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AttendanceRepository attendanceRepository;

    public StudentService(StudentRepository studentRepository,
                          DepartmentRepository departmentRepository,
                          EnrollmentRepository enrollmentRepository,
                          AttendanceRepository attendanceRepository) {
        this.studentRepository = studentRepository;
        this.departmentRepository = departmentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.attendanceRepository = attendanceRepository;
    }

    public List<Student> getAll(String search) {
        return studentRepository.findAll(search);
    }

    public Student getById(int id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student with ID " + id + " not found."));
    }

    public Student create(Student input) {
        validate(input, null);
        int newId = studentRepository.insert(input);
        return getById(newId);
    }

    public Student update(int id, Student input) {
        if (!studentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Student with ID " + id + " not found.");
        }
        validate(input, id);
        studentRepository.update(id, input);
        return getById(id);
    }

    /**
     * Deletes the student's enrollments and then the student.
     * @Transactional makes both DELETE statements one transaction:
     * if the second fails, the first is rolled back.
     */
    @Transactional
    public void delete(int id) {
        if (!studentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Student with ID " + id + " not found.");
        }
        enrollmentRepository.deleteByStudent(id);
        studentRepository.delete(id);
    }

    /** Session-wise attendance (stored procedure) plus the percentage (stored function). */
    public Map<String, Object> attendance(int id) {
        Student student = getById(id);
        List<AttendanceRecord> records = attendanceRepository.studentAttendance(id);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("Student_ID", student.getStudentId());
        result.put("Name", student.getName());
        result.put("Attendance_Pct", attendanceRepository.attendancePercentage(id));
        result.put("Sessions", records);
        return result;
    }

    private void validate(Student s, Integer currentId) {
        s.setName(InputValidator.requireText(s.getName(), "Name", 50));
        s.setGender(InputValidator.optionalText(s.getGender(), "Gender", 10));
        s.setEmail(InputValidator.optionalEmail(s.getEmail(), "Email"));
        s.setMobileNumber(InputValidator.optionalPhone(s.getMobileNumber(), "Mobile_Number"));
        InputValidator.optionalRange(s.getCgpa(), "CGPA", 0, 10);

        if (s.getDob() != null && !s.getDob().isBefore(LocalDate.now())) {
            throw new BadRequestException("DOB must be a date in the past.");
        }
        if (s.getDeptId() != null && !departmentRepository.existsById(s.getDeptId())) {
            throw new BadRequestException("Department with ID " + s.getDeptId() + " does not exist.");
        }
        if (s.getEmail() != null && studentRepository.emailInUse(s.getEmail(), currentId)) {
            throw new ConflictException("Email " + s.getEmail() + " is already used by another student.");
        }
    }
}
