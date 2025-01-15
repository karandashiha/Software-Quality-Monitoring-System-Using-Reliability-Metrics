package com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.service.entity;

import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity.CommitMetric;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity.Employee;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.repository.CommitMetricRepository;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.repository.EmployeeRepository;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.service.git.LocalGitService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class EmployeeService {

    private final CommitMetricRepository commitMetricRepository;
    private final EmployeeRepository employeeRepository;
    private final LocalGitService localGitService;

    public EmployeeService(EmployeeRepository employeeRepository,
                           CommitMetricRepository commitMetricRepository,
                           LocalGitService localGitService) {
        this.employeeRepository = employeeRepository;
        this.localGitService = localGitService;
        this.commitMetricRepository=commitMetricRepository;
    }

    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Співробітника не знайдено з ID: " + id));
    }

    public Employee getEmployeeByName(String name) {
        return employeeRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Співробітника не знайдено з ім'ям: " + name));
    }


    // Отримати всіх працівників, які брали участь у проекті
    public List<Employee> getEmployeesByProjectName(String projectName) {
    List<CommitMetric> commitMetrics = commitMetricRepository.findByProjectName(projectName);

    // Створюємо множину для унікальних працівників, щоб уникнути дублікатів
    Set<Employee> employees = new HashSet<>();
    for (CommitMetric commit : commitMetrics) {
        employees.add(commit.getAuthor()); // Додаємо працівників, які брали участь у проекті
    }
    return new ArrayList<>(employees); // Повертаємо список працівників
    }

    public Map<String, Long> getContribution(String projectName, Employee author) {
        return localGitService.getEmployeeContribution(projectName, author);
    }

    // Оновлення ставок на основі ефективності
    public void updateEmployeeHourlyRate(Employee employee) {
        // Розрахунок ефективності
        double efficiency = calculateEmployeeEfficiency(employee);

        // Збільшення ставки залежно від ефективності
        if (efficiency > 50) {
            employee.setHourlyRate(employee.getHourlyRate() + 10.0);  // Збільшення ставки на 10
        } else if (efficiency < 20) {
            employee.setHourlyRate(employee.getHourlyRate() - 5.0);  // Зменшення ставки на 5
        }

        // Зберігаємо оновлену інформацію
        employeeRepository.save(employee);
    }

    // Метод для обчислення ефективності працівника
    public double calculateEmployeeEfficiency(Employee employee) {
        List<CommitMetric> commitMetrics = commitMetricRepository.findByAuthorId(employee.getId());

        int totalLinesAdded = 0;
        for (CommitMetric commit : commitMetrics) {
            totalLinesAdded += commit.getLinesAdded();
        }

        int totalCommits = commitMetrics.size();

        if (totalCommits > 0) {
            return (double) totalLinesAdded / totalCommits;  // Середнє число рядків на один коміт
        } else {
            return 0;  // Якщо комітів немає, ефективність 0
        }
    }
}
