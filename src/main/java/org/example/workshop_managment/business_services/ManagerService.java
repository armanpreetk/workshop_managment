package org.example.workshop_managment.business_services;

import org.example.workshop_managment.model.Employee;
import org.example.workshop_managment.model.Manager;
import org.example.workshop_managment.repository.EmployeeRepository;
import org.example.workshop_managment.repository.ManagerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Service
public class ManagerService {
    private static final Logger log = LoggerFactory.getLogger(ManagerService.class);

    private final ManagerRepository managerRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public ManagerService(ManagerRepository managerRepository, EmployeeRepository employeeRepository) {
        this.managerRepository = managerRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional
    public Manager createManager(Manager manager) {
        if (manager.getId() != null) {
            manager.setId(null);
        }
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
        Manager existingManager = managerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Manager not found with id " + id));
            
        existingManager.setName(managerDetails.getName());
        existingManager.setEmail(managerDetails.getEmail());
        existingManager.setPassword(managerDetails.getPassword());
            
        return managerRepository.save(existingManager);
    }

    @Transactional
    public void deleteManager(Long id) {
        log.debug("Attempting to delete manager with ID: {}", id);
        
        Manager manager = managerRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Manager not found with ID: {}", id);
                    return new RuntimeException("Manager not found with id " + id);
                });
        
        if (hasEmployees(id)) {
            log.warn("Cannot delete manager with ID: {} because they have assigned employees", id);
            throw new RuntimeException("Cannot delete manager with assigned employees");
        }
        
        if (manager.getAdmin() != null) {
            log.debug("Removing manager with ID: {} from admin with ID: {}", 
                     id, manager.getAdmin().getId());
            manager.getAdmin().removeManager(manager);
        }
        
        log.info("Deleting manager with ID: {}", id);
        managerRepository.delete(manager);
    }

    public boolean hasEmployees(Long managerId) {
        List<Employee> employees = employeeRepository.findByManager_Id(managerId);
        boolean hasEmployees = employees.size() > 0;
        if (hasEmployees) {
            log.debug("Manager with ID: {} has {} employees", managerId, employees.size());
        }
        return hasEmployees;
    }
    
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
    public Employee addEmployeeToManager(Manager manager, Employee employee) {
        if (employee.getId() != null) {
            employee.setId(null);
        }
        employee.setManager(manager);
        return employeeRepository.save(employee);
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