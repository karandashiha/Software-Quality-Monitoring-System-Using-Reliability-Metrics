package com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity.DTO;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EmployeeDTO {
    private Long id;
    private String name;
    private Double hourlyRate;
    private String role;
    private LocalDate dateJoined;
}
