package com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.controller;

import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity.Employee;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.service.entity.EmployeeService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    // Отримати всіх працівників, які працюють над проектом
    @GetMapping("/project/{projectName}")
    public ResponseEntity<List<Employee>> getEmployeesByProject(@PathVariable String projectName) {
        List<Employee> employees = employeeService.getEmployeesByProjectName(projectName);
        if (employees.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.emptyList()); // Якщо немає працівників, повертаємо 404
        }
        return ResponseEntity.ok(employees); // Повертаємо список працівників
    }

    // Отримати працівника за ім'ям
    @GetMapping("/{name}")
    public ResponseEntity<Employee> getEmployee(@PathVariable String name) {
        return ResponseEntity.ok(employeeService.getEmployeeByName(name));
    }

    // Оновити ставку працівника на основі ефективності
    @PutMapping("/{id}/update-rate")
    public ResponseEntity<String> updateEmployeeHourlyRate(@PathVariable Long id) {
        try {
            Employee employee = employeeService.getEmployeeById(id);
            double oldRate = employee.getHourlyRate();
            employeeService.updateEmployeeHourlyRate(employee);
            double newRate = employee.getHourlyRate();
            String responseMessage = "Ставка працівника з ID " + id + " оновлена успішно. "
                    + "Стара ставка: " + oldRate + ", нова ставка: " + newRate;
            return ResponseEntity.ok(responseMessage);
        } catch (EntityNotFoundException e) {
            String errorMessage = "Помилка: працівника з ID " + id + " не знайдено.";
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
    }

    // Отримати ефективність працівника
    @GetMapping("/{id}/efficiency")
    public ResponseEntity<String> getEmployeeEfficiency(@PathVariable Long id) {
        try {
            Employee employee = employeeService.getEmployeeById(id);
            double efficiency = employeeService.calculateEmployeeEfficiency(employee);
            return ResponseEntity.ok("Ефективність працівника з ID " + id + ": " + efficiency + " доданих рядків");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Працівника з ID " + id + " не знайдено.");
        }
    }

    // Отримати внесок працівника в проект
    @GetMapping("/{id}/contribution")
    public ResponseEntity<String> getEmployeeContribution(
            @PathVariable Long id,
            @RequestParam String projectName) {
        Employee employee = employeeService.getEmployeeById(id);
        Map<String, Long> contribution = employeeService.getContribution(projectName, employee);

        StringBuilder responseMessage = new StringBuilder();
        responseMessage.append("Внесок працівника з ID ").append(id)
                .append(" (").append(employee.getName()).append(") у проект '")
                .append(projectName).append("':\n");

        if (contribution.isEmpty()) {
            responseMessage.append("Немає даних про внесок для вказаного проекту.");
        } else {
            contribution.forEach((fileType, count) ->
                    responseMessage.append("- Тип файлу: ").append(fileType)
                            .append(", кількість комітів: ").append(count).append("\n"));
        }

        return ResponseEntity.ok(responseMessage.toString());
    }

}
