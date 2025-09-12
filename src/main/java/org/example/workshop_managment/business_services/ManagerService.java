package org.example.workshop_managment.business_services;

import org.example.workshop_managment.model.Employee;
import org.example.workshop_managment.model.Manager;
import org.example.workshop_managment.repository.EmployeeRepository;
import org.example.workshop_managment.repository.ManagerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ManagerService {

    private final ManagerRepository managerRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public ManagerService(ManagerRepository managerRepository, EmployeeRepository employeeRepository) {
        this.managerRepository = managerRepository;
        this.employeeRepository = employeeRepository;
    }

    // CRUD Operations
    @Transactional
    public Manager createManager(Manager manager) {
        return managerRepository.save(manager);
    }

    public List<Manager> getAllManagers() {
        return managerRepository.findAll();
    }

    public Optional<Manager> getManagerById(Long id) {
        return managerRepository.findById(id);
    }

    @Transactional
    public Manager updateManager(Long id, Manager managerDetails) {
        return managerRepository.findById(id).map(manager -> {
            manager.setName(managerDetails.getName());
            manager.setEmail(managerDetails.getEmail());
            manager.setPassword(managerDetails.getPassword());
            return managerRepository.save(manager);
        }).orElseThrow(() -> new RuntimeException("Manager not found with id " + id));
    }

    @Transactional
    public void deleteManager(Long id) {
        managerRepository.deleteById(id);
    }

    // Employee Management for Manager
    @Transactional
    public Manager addEmployeeToManager(Long managerId, Long employeeId) {
        Manager manager = managerRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found with id " + managerId));
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with id " + employeeId));
        manager.addEmployee(employee);
        return managerRepository.save(manager);
    }

    @Transactional
    public Manager removeEmployeeFromManager(Long managerId, Long employeeId) {
        Manager manager = managerRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found with id " + managerId));
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with id " + employeeId));
        manager.removeEmployee(employee);
        return managerRepository.save(manager);
    }
}