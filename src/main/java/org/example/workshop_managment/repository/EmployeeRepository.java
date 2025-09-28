package org.example.workshop_managment.repository;

import org.example.workshop_managment.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
	Employee findByName(String name);
	List<Employee> findByManager_Id(Long managerId);
}
