package org.example.workshop_managment.controller;

import org.example.workshop_managment.model.Employee;
import org.example.workshop_managment.model.Manager;
import org.example.workshop_managment.model.Admin;
import org.example.workshop_managment.repository.AdminRepository;
import org.example.workshop_managment.repository.EmployeeRepository;
import org.example.workshop_managment.repository.ManagerRepository;
import org.example.workshop_managment.business_services.ManagerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/managers")
public class ManagerController {
    private final ManagerRepository managers;
    private final EmployeeRepository employees;
    private final AdminRepository admins;
    private final ManagerService managerService;

    public ManagerController(ManagerRepository managers, EmployeeRepository employees, AdminRepository admins, ManagerService managerService) {
        this.managers = managers;
        this.employees = employees;
        this.admins = admins;
        this.managerService = managerService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Manager> list() { return managers.findAll(); }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@Valid @RequestBody Manager manager, @RequestParam(value = "adminId", required = false) Long adminId) {
        Admin selectedAdmin = null;
        if (adminId != null) {
            selectedAdmin = admins.findById(adminId).orElse(null);
            if (selectedAdmin == null) return ResponseEntity.notFound().build();
        } else {
            selectedAdmin = admins.findByName("admin");
            if (selectedAdmin == null) {
                return ResponseEntity.status(500).body("Default admin not found. Ensure admin.enforce-single=true at startup.");
            }
        }

        manager.setAdmin(selectedAdmin);
        Manager saved = managerService.createManager(manager);
        return ResponseEntity.created(URI.create("/api/managers/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody Manager payload) {
        try {
            Manager updated = managerService.updateManager(id, payload);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/me/employees")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> myEmployees(Authentication auth) {
        String name = auth.getName();
        Manager me = managers.findByName(name);
        if (me == null) return ResponseEntity.status(404).body("Manager not found");
        return ResponseEntity.ok(employees.findByManager_Id(me.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Manager> get(@PathVariable Long id) {
        return managers.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/employees")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> addEmployee(@PathVariable Long id, @Valid @RequestBody Employee employee, Authentication auth) {
        return managers.findById(id).map(manager -> {
            if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"))) {
                Manager me = managers.findByName(auth.getName());
                if (me == null || !me.getId().equals(manager.getId())) {
                    return ResponseEntity.status(403).build();
                }
            }
            
            if (employee.getId() != null) {
                employee.setId(null);
            }
            
            try {
                Employee saved = managerService.addEmployeeToManager(manager, employee);
                return ResponseEntity.created(URI.create("/api/employees/" + saved.getId())).body(saved);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            if (!managers.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            
            managerService.deleteManager(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("employees")) {
                return ResponseEntity.status(409).body(e.getMessage());
            }
            return ResponseEntity.status(500).body("Error deleting manager: " + e.getMessage());
        }
    }
}
