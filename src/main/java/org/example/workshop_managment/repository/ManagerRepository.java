package org.example.workshop_managment.repository;

import org.example.workshop_managment.model.Manager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface ManagerRepository extends JpaRepository<Manager, Long> {}
