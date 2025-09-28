package org.example.workshop_managment.controller;

import org.example.workshop_managment.model.Admin;
import org.example.workshop_managment.model.Manager;
import org.example.workshop_managment.model.Employee;
import org.example.workshop_managment.repository.AdminRepository;
import org.example.workshop_managment.repository.ManagerRepository;
import org.example.workshop_managment.repository.EmployeeRepository;
import org.example.workshop_managment.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class JwtAuthController {
    @Autowired
    private AdminRepository adminRepo;
    @Autowired
    private ManagerRepository managerRepo;
    @Autowired
    private EmployeeRepository employeeRepo;
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> req) {
        String name = req.get("name");
        String password = req.get("password");
        Object user = null;
        String type = null;
        Admin admin = adminRepo.findByName(name);
        if (admin != null && admin.getPassword().equals(password)) {
            user = admin;
            type = "ADMIN";
        }
        if (user == null) {
            Manager manager = managerRepo.findByName(name);
            if (manager != null && manager.getPassword().equals(password)) {
                user = manager;
                type = "MANAGER";
            }
        }
        if (user == null) {
            Employee employee = employeeRepo.findByName(name);
            if (employee != null && employee.getPassword().equals(password)) {
                user = employee;
                type = "EMPLOYEE";
            }
        }
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", getId(user));
        claims.put("name", name);
        claims.put("type", type);
        String token = jwtUtil.generateToken(claims, name);
        Map<String, Object> resp = new HashMap<>();
        resp.put("token", token);
        resp.put("user", claims);
        return ResponseEntity.ok(resp);
    }

    private Long getId(Object user) {
        if (user instanceof Admin) return ((Admin)user).getId();
        if (user instanceof Manager) return ((Manager)user).getId();
        if (user instanceof Employee) return ((Employee)user).getId();
        return null;
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Missing token"));
        }
        String token = authHeader.substring(7);
        Map<String, Object> claims = jwtUtil.getClaims(token);
        return ResponseEntity.ok(Map.of("user", claims));
    }
}
