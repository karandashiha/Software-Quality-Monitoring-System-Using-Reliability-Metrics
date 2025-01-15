package com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.repository;

import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByName(String name);
    long count();  // Підрахунок кількості працівників
    boolean existsByName(String authorName);
}
