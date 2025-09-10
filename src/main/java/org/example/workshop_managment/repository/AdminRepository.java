package org.example.workshop_managment.repository;

import org.example.workshop_managment.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, Long> {}
