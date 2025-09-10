package org.example.workshop_managment.controller;

import org.example.workshop_managment.model.Employee;
import org.example.workshop_managment.model.Manager;
import org.example.workshop_managment.model.Admin;
import org.example.workshop_managment.repository.AdminRepository;
import org.example.workshop_managment.repository.EmployeeRepository;
import org.example.workshop_managment.repository.ManagerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/managers")
public class ManagerController {
    private final ManagerRepository managers;
    private final EmployeeRepository employees;
    private final AdminRepository admins;

    public ManagerController(ManagerRepository managers, EmployeeRepository employees, AdminRepository admins) {
        this.managers = managers;
        this.employees = employees;
        this.admins = admins;
    }

    @GetMapping
    public List<Manager> list() { return managers.findAll(); }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Manager manager, @RequestParam(value = "adminId", required = false) Long adminId) {
        // Determine admin
        Long resolvedAdminId = adminId != null ? adminId : (manager.getAdmin() != null ? manager.getAdmin().getId() : null);
        if (resolvedAdminId == null) {
            return ResponseEntity.badRequest().body("adminId is required (query param) or include admin.id in the JSON body");
        }

        return admins.findById(resolvedAdminId)
                .map((Admin admin) -> {
                    manager.setAdmin(admin);
                    Manager saved = managers.save(manager);
                    return ResponseEntity.created(URI.create("/api/managers/" + saved.getId())).body(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Manager> get(@PathVariable Long id) {
        return managers.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/employees")
    public ResponseEntity<Employee> addEmployee(@PathVariable Long id, @RequestBody Employee employee) {
        return managers.findById(id).map(manager -> {
            employee.setManager(manager);
            Employee saved = employees.save(employee);
            return ResponseEntity.created(URI.create("/api/employees/" + saved.getId())).body(saved);
        }).orElse(ResponseEntity.notFound().build());
    }
}
