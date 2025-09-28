package org.example.workshop_managment.repository;

import org.example.workshop_managment.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    @Query("select t from Task t join fetch t.employee e where e.id = :employeeId")
    List<Task> findByEmployee_Id(@Param("employeeId") Long employeeId);

    @Query("select t from Task t join fetch t.employee e where e.manager.id = :managerId")
    List<Task> findByManagerId(@Param("managerId") Long managerId);
}
