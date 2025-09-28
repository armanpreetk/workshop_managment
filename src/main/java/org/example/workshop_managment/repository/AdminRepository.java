package org.example.workshop_managment.repository;

import org.example.workshop_managment.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
public interface AdminRepository extends JpaRepository<Admin, Long> {
	Admin findByName(String name);
}
