package org.example.workshop_managment.controller;

import org.example.workshop_managment.model.Employee;
import org.example.workshop_managment.model.Manager;
import org.example.workshop_managment.model.Task;
import org.example.workshop_managment.repository.EmployeeRepository;
import org.example.workshop_managment.repository.ManagerRepository;
import org.example.workshop_managment.business_services.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;
    private final ManagerRepository managers;
    private final EmployeeRepository employees;

    public TaskController(TaskService taskService, ManagerRepository managers, EmployeeRepository employees) {
        this.taskService = taskService;
        this.managers = managers;
        this.employees = employees;
    }

    // Manager assigns a task to an employee in his team
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> assign(Authentication auth, @RequestBody Map<String, Object> payload) {
        String name = auth.getName();
        Manager me = managers.findByName(name);
        if (me == null) return ResponseEntity.status(404).body("Manager not found");
        Long employeeId = payload.get("employeeId") instanceof Number ? ((Number)payload.get("employeeId")).longValue() : null;
        String title = (String) payload.get("title");
        String description = (String) payload.get("description");
        OffsetDateTime dueDate = null;
        if (payload.get("dueDate") instanceof String s && !s.isBlank()) {
            try {
                // Prefer full ISO with offset
                dueDate = OffsetDateTime.parse(s);
            } catch (Exception e1) {
                // Support 'yyyy-MM-ddTHH:mm' (datetime-local) by assuming local timezone
                try {
                    java.time.LocalDateTime ldt = java.time.LocalDateTime.parse(s);
                    dueDate = ldt.atZone(java.time.ZoneId.systemDefault()).toOffsetDateTime();
                } catch (Exception ignored) {}
            }
        }
        if (employeeId == null || title == null || title.isBlank()) {
            return ResponseEntity.badRequest().body("employeeId and title are required");
        }
        try {
            Task t = taskService.assignTask(me.getId(), employeeId, title, description, dueDate);
            return ResponseEntity.created(URI.create("/api/tasks/" + t.getId())).body(t);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Manager lists tasks for his team
    @GetMapping("/manager/me")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> myTeamTasks(Authentication auth) {
        String name = auth.getName();
        Manager me = managers.findByName(name);
        if (me == null) return ResponseEntity.status(404).body("Manager not found");
        List<Task> list = taskService.listTasksForManager(me.getId());
        return ResponseEntity.ok(list);
    }

    // Employee lists his tasks
    @GetMapping("/employee/me")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<?> myTasks(Authentication auth) {
        String name = auth.getName();
        Employee me = employees.findByName(name);
        if (me == null) return ResponseEntity.status(404).body("Employee not found");
        List<Task> list = taskService.listTasksForEmployee(me.getId());
        return ResponseEntity.ok(list);
    }

    // Employee marks a task as done
    @PostMapping("/{id}/done")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<?> done(@PathVariable Long id, Authentication auth) {
        String name = auth.getName();
        Employee me = employees.findByName(name);
        if (me == null) return ResponseEntity.status(404).body("Employee not found");
        try {
            Task t = taskService.markDone(me.getId(), id);
            return ResponseEntity.ok(t);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not belong")) return ResponseEntity.status(403).body(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
