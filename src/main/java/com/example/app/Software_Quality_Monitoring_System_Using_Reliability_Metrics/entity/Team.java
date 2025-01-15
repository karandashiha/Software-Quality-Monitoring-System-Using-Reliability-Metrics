package com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "teams")
@Getter
@Setter
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "team_lead_id", nullable = false)
    private Employee teamLead;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Employee member;

    @Column(name = "project_name")
    private String projectName;

    @Column(name = "repository_path" , nullable = false)
    private String repositoryPath;

    @ManyToOne
    @JoinColumn(name = "project_metrics_id", nullable = false) // Додаємо зв'язок до ProjectMetrics
    private ProjectMetrics projectMetrics;

}