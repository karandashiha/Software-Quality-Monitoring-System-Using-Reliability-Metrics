package com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeamDTO {
    private Long id;
    private EmployeeDTO teamLead;
    private EmployeeDTO member;
    private String projectName;
    private String repositoryPath;

}
