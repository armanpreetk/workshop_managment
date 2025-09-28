package org.example.workshop_managment.config;

import org.example.workshop_managment.repository.AdminRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(prefix = "maintenance", name = "delete-admins", havingValue = "true")
public class AdminDeletionTask implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminDeletionTask.class);

    private final AdminRepository adminRepository;

    public AdminDeletionTask(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        long count = adminRepository.count();
        adminRepository.deleteAll();
        log.warn("Deleted {} admin record(s) due to maintenance.delete-admins=true", count);
    }
}
