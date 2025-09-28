package org.example.workshop_managment.config;

import org.example.workshop_managment.model.Admin;
import org.example.workshop_managment.repository.AdminRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(prefix = "admin", name = "enforce-single", havingValue = "true", matchIfMissing = true)
public class AdminInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    private final AdminRepository adminRepository;

    public AdminInitializer(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        List<Admin> all = adminRepository.findAll();
        Admin primary = null;
        List<Admin> toDelete = new ArrayList<>();

        for (Admin a : all) {
            if (a.getName() != null && a.getName().equalsIgnoreCase("admin")) {
                if (primary == null) {
                    primary = a;
                } else {
                    toDelete.add(a);
                }
            } else {
                toDelete.add(a);
            }
        }

        if (primary == null) {
            primary = new Admin("admin", "admin@example.com", "admin");
            adminRepository.save(primary);
            log.info("Created default admin user 'admin'.");
        }
        else {
            primary.setName("admin");
            primary.setPassword("admin");
            if (primary.getEmail() == null || primary.getEmail().isBlank()) {
                primary.setEmail("admin@example.com");
            }
            adminRepository.save(primary);
            log.info("Ensured admin credentials are normalized for user 'admin'.");
        }

        if (!toDelete.isEmpty()) {
            int count = toDelete.size();
            adminRepository.deleteAll(toDelete);
            log.warn("Deleted {} other admin record(s) to enforce single 'admin' user.", count);
        }
    }
}
