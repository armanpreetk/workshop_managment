package org.example.workshop_managment.controller;

import org.example.workshop_managment.model.Employee;
import org.example.workshop_managment.model.Manager;
import org.example.workshop_managment.repository.EmployeeRepository;
import org.example.workshop_managment.repository.ManagerRepository;
import org.example.workshop_managment.business_services.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    private final EmployeeRepository employees;
    private final ManagerRepository managers;
    private final EmployeeService employeeService;

    public EmployeeController(EmployeeRepository employees, ManagerRepository managers, EmployeeService employeeService) {
        this.employees = employees;
        this.managers = managers;
        this.employeeService = employeeService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Employee> list() { return employees.findAll(); }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> create(@Valid @RequestBody Employee employee,
                                    @RequestParam(value = "managerId", required = false) Long managerId,
                                    Authentication auth) {
        Manager assignedManager = null;
        if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"))) {
            Manager me = managers.findByName(auth.getName());
            if (me == null) return ResponseEntity.status(404).body("Manager not found");
            assignedManager = me;
        } else {
            if (managerId == null) {
                return ResponseEntity.badRequest().body("managerId is required when creating an employee as ADMIN");
            }
            assignedManager = managers.findById(managerId).orElse(null);
            if (assignedManager == null) return ResponseEntity.notFound().build();
        }

        employee.setManager(assignedManager);
        Employee saved = employeeService.createEmployee(employee);
        return ResponseEntity.created(URI.create("/api/employees/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody Employee payload, Authentication auth) {
        return employees.findById(id).map(existing -> {
            if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"))) {
                Manager me = managers.findByName(auth.getName());
                if (me == null || existing.getManager() == null || !existing.getManager().getId().equals(me.getId())) {
                    return ResponseEntity.status(403).build();
                }
            }
            try {
                Employee updated = employeeService.updateEmployee(id, payload);
                return ResponseEntity.ok(updated);
            } catch (RuntimeException e) {
                return ResponseEntity.notFound().build();
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication auth) {
        return employees.findById(id).map(existing -> {
            if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"))) {
                Manager me = managers.findByName(auth.getName());
                if (me == null || existing.getManager() == null || !existing.getManager().getId().equals(me.getId())) {
                    return ResponseEntity.status(403).build();
                }
            }
            employeeService.deleteEmployee(id);
            return ResponseEntity.noContent().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public ResponseEntity<Employee> get(@PathVariable Long id) {
        return employees.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication auth) {
        return ResponseEntity.ok(auth.getPrincipal());
    }

    @GetMapping("/me/profile")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<?> myProfile(Authentication auth) {
        String name = auth.getName();
        Employee me = employees.findByName(name);
        if (me == null) return ResponseEntity.status(404).body("Employee not found");
        return ResponseEntity.ok(me);
    }

    @GetMapping("/me/team")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<?> myTeam(Authentication auth) {
        String name = auth.getName();
        Employee me = employees.findByName(name);
        if (me == null || me.getManager() == null) return ResponseEntity.status(404).body("Manager not assigned");
        return ResponseEntity.ok(employees.findByManager_Id(me.getManager().getId()));
    }
}
