package org.example.workshop_managment.business_services;

import org.example.workshop_managment.model.Employee;
import org.example.workshop_managment.model.Manager;
import org.example.workshop_managment.model.Task;
import org.example.workshop_managment.repository.EmployeeRepository;
import org.example.workshop_managment.repository.ManagerRepository;
import org.example.workshop_managment.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class TaskService {
    private final TaskRepository tasks;
    private final EmployeeRepository employees;
    private final ManagerRepository managers;

    public TaskService(TaskRepository tasks, EmployeeRepository employees, ManagerRepository managers) {
        this.tasks = tasks;
        this.employees = employees;
        this.managers = managers;
    }

    @Transactional
    public Task assignTask(Long managerId, Long employeeId, String title, String description, OffsetDateTime dueDate) {
        Manager manager = managers.findById(managerId).orElseThrow(() -> new RuntimeException("Manager not found"));
        Employee employee = employees.findById(employeeId).orElseThrow(() -> new RuntimeException("Employee not found"));
        if (employee.getManager() == null || !employee.getManager().getId().equals(manager.getId())) {
            throw new RuntimeException("Employee does not belong to this manager");
        }
        Task t = new Task(title, description, employee, dueDate);
        return tasks.save(t);
    }

    public List<Task> listTasksForManager(Long managerId) {
        return tasks.findByManagerId(managerId);
    }

    public List<Task> listTasksForEmployee(Long employeeId) {
        return tasks.findByEmployee_Id(employeeId);
    }

    @Transactional
    public Task markDone(Long employeeId, Long taskId) {
        Task t = tasks.findById(taskId).orElseThrow(() -> new RuntimeException("Task not found"));
        if (!t.getEmployee().getId().equals(employeeId)) {
            throw new RuntimeException("Task does not belong to this employee");
        }
        t.setStatus("DONE");
        return tasks.save(t);
    }
}
