package com.trainingmanagement.service;

import com.trainingmanagement.exception.BadRequestException;
import com.trainingmanagement.exception.ConflictException;
import com.trainingmanagement.exception.ResourceNotFoundException;
import com.trainingmanagement.model.Department;
import com.trainingmanagement.repository.DepartmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/** Business rules for departments. */
@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public List<Department> getAll() {
        return departmentRepository.findAll();
    }

    public Department getById(int id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department with ID " + id + " not found."));
    }

    public Department create(Department input) {
        Integer id = InputValidator.require(input.getDeptId(), "Dept_ID");
        if (id <= 0) {
            throw new BadRequestException("Dept_ID must be a positive number.");
        }
        if (departmentRepository.existsById(id)) {
            throw new ConflictException("Department ID " + id + " already exists.");
        }
        clean(input);
        departmentRepository.insert(input);
        return getById(id);
    }

    public Department update(int id, Department input) {
        if (!departmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Department with ID " + id + " not found.");
        }
        clean(input);
        departmentRepository.update(id, input);
        return getById(id);
    }

    public void delete(int id) {
        if (!departmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Department with ID " + id + " not found.");
        }
        int students = departmentRepository.countStudents(id);
        if (students > 0) {
            throw new ConflictException("Cannot delete this department: " + students
                    + " student(s) belong to it. Move or delete those students first.");
        }
        departmentRepository.delete(id);
    }

    private void clean(Department d) {
        d.setDeptName(InputValidator.requireText(d.getDeptName(), "Dept_Name", 50));
        d.setHodName(InputValidator.optionalText(d.getHodName(), "HOD_Name", 50));
    }
}
