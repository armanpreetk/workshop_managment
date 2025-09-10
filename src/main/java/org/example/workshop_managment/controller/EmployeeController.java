package org.example.workshop_managment.controller;

import org.example.workshop_managment.model.Employee;
import org.example.workshop_managment.repository.EmployeeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    private final EmployeeRepository employees;

    public EmployeeController(EmployeeRepository employees) {
        this.employees = employees;
    }

    @GetMapping
    public List<Employee> list() { return employees.findAll(); }

    @PostMapping
    public ResponseEntity<Employee> create(@RequestBody Employee employee) {
        Employee saved = employees.save(employee);
        return ResponseEntity.created(URI.create("/api/employees/" + saved.getId())).body(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> get(@PathVariable Long id) {
        return employees.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}
