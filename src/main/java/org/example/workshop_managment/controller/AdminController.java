package org.example.workshop_managment.controller;

import org.example.workshop_managment.model.Admin;
import org.example.workshop_managment.model.Manager;
import org.example.workshop_managment.repository.AdminRepository;
import org.example.workshop_managment.repository.ManagerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/admins")
public class AdminController {
    private final AdminRepository admins;
    private final ManagerRepository managers;

    public AdminController(AdminRepository admins, ManagerRepository managers) {
        this.admins = admins;
        this.managers = managers;
    }

    @GetMapping
    public List<Admin> list() { return admins.findAll(); }

    @PostMapping
    public ResponseEntity<Admin> create(@RequestBody Admin admin) {
        Admin saved = admins.save(admin);
        return ResponseEntity.created(URI.create("/api/admins/" + saved.getId())).body(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Admin> get(@PathVariable Long id) {
        return admins.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/managers")
    public ResponseEntity<Manager> addManager(@PathVariable Long id, @RequestBody Manager manager) {
        return admins.findById(id).map(admin -> {
            manager.setAdmin(admin);
            Manager saved = managers.save(manager);
            return ResponseEntity.created(URI.create("/api/managers/" + saved.getId())).body(saved);
        }).orElse(ResponseEntity.notFound().build());
    }
}
