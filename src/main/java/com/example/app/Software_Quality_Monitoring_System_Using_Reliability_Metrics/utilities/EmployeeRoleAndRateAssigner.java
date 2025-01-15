package com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.utilities;

import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity.Employee;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity.Role;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.repository.EmployeeRepository;
import org.springframework.stereotype.Component;

@Component
public class EmployeeRoleAndRateAssigner {

    private final EmployeeRepository employeeRepository;

    public EmployeeRoleAndRateAssigner(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

   public void assignRoleAndRate(Employee employee, boolean isLead) {
       if (isLead) {
           employee.setRole(Role.TeamLead);
           employee.setHourlyRate(50.0);
       } else {
           employee.setRole(Role.Developer);
           employee.setHourlyRate(30.0);
       }
       employeeRepository.save(employee);
   }

}