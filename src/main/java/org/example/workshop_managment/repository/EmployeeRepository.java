package org.example.workshop_managment.repository;

import org.example.workshop_managment.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {}
