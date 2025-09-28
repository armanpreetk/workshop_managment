package org.example.workshop_managment.business_services;

import org.example.workshop_managment.model.Admin;
import org.example.workshop_managment.model.Manager;
import org.example.workshop_managment.repository.AdminRepository;
import org.example.workshop_managment.repository.ManagerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    private final AdminRepository adminRepository;
    private final ManagerRepository managerRepository;

    @Autowired
    public AdminService(AdminRepository adminRepository, ManagerRepository managerRepository) {
        this.adminRepository = adminRepository;
        this.managerRepository = managerRepository;
    }

    @Transactional
    public Admin createAdmin(Admin admin) {
        return adminRepository.save(admin);
    }

    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    public Optional<Admin> getAdminById(Long id) {
        return adminRepository.findById(id);
    }

    @Transactional
    public Admin updateAdmin(Long id, Admin adminDetails) {
        return adminRepository.findById(id).map(admin -> {
            admin.setName(adminDetails.getName());
            admin.setEmail(adminDetails.getEmail());
            admin.setPassword(adminDetails.getPassword());
            return adminRepository.save(admin);
        }).orElseThrow(() -> new RuntimeException("Admin not found with id " + id));
    }

    @Transactional
    public void deleteAdmin(Long id) {
        adminRepository.deleteById(id);
    }

    @Transactional
    public Admin addManagerToAdmin(Long adminId, Long managerId) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found with id " + adminId));
        Manager manager = managerRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found with id " + managerId));
        admin.addManager(manager);
        return adminRepository.save(admin);
    }

    @Transactional
    public Admin removeManagerFromAdmin(Long adminId, Long managerId) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found with id " + adminId));
        Manager manager = managerRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found with id " + managerId));
        admin.removeManager(manager);
        return adminRepository.save(admin);
    }
}