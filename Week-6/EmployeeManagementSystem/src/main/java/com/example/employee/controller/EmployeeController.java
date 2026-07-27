package com.example.employee.controller;

import com.example.employee.dto.EmployeeDto;
import com.example.employee.model.Department;
import com.example.employee.model.Employee;
import com.example.employee.projection.EmployeeSummary;
import com.example.employee.repository.DepartmentRepository;
import com.example.employee.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/employees")
public class EmployeeController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @GetMapping
    public List<Employee> getAll() {
        return employeeRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<Employee> create(@RequestParam Long departmentId, @RequestBody Employee employee) {
        return departmentRepository.findById(departmentId).map(dept -> {
            employee.setDepartment(dept);
            return ResponseEntity.ok(employeeRepository.save(employee));
        }).orElse(ResponseEntity.badRequest().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> getById(@PathVariable Long id) {
        return employeeRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Employee> update(@PathVariable Long id, @RequestBody Employee details) {
        return employeeRepository.findById(id).map(emp -> {
            emp.setName(details.getName());
            emp.setEmail(details.getEmail());
            return ResponseEntity.ok(employeeRepository.save(emp));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (employeeRepository.existsById(id)) {
            employeeRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Dynamic Search with Pagination & Sorting
    @GetMapping("/search")
    public Page<Employee> search(
            @RequestParam(required = false, defaultValue = "") String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return employeeRepository.findByNameContaining(name, pageable);
    }

    // Custom Named Query Endpoint
    @GetMapping("/by-email-named")
    public ResponseEntity<Employee> getByEmailNamed(@RequestParam String email) {
        Employee emp = employeeRepository.findByEmailNamed(email);
        return emp != null ? ResponseEntity.ok(emp) : ResponseEntity.notFound().build();
    }

    // Interface-based Projection Endpoint
    @GetMapping("/projections/summary")
    public List<EmployeeSummary> getSummaries(@RequestParam Long departmentId) {
        return employeeRepository.findSummariesByDepartmentId(departmentId);
    }

    // Class-based Projection DTO Endpoint
    @GetMapping("/projections/dto")
    public List<EmployeeDto> getDtos(@RequestParam Long departmentId) {
        return employeeRepository.findDtosByDepartmentId(departmentId);
    }
}
