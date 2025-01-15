package com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Entity
@Table(name = "commit_metrics", indexes = {
        @Index(name = "idx_project_name", columnList = "project_name")
})

@Getter
@Setter
public class CommitMetric {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "commit_hash", nullable = false)
    private String commitHash;

    @ManyToOne
    @JoinColumn(name = "author_id", referencedColumnName = "id")
    private Employee author;

    @Column(name = "commit_date", nullable = false)
    private LocalDateTime commitDate;

    @Column(name = "lines_added", nullable = false)
    private int linesAdded;

    @Column(name = "lines_deleted", nullable = false)
    private int linesDeleted;

    @Column(name = "project_name")
    private String projectName;

    @Column(name = "file_type")
    private String fileType;

    // Додаємо посилання на ProjectMetrics
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_metrics_id") // referencedColumnName = "id") Назва стовпця в таблиці commit_metrics
    private ProjectMetrics projectMetrics; // Зв'язок з ProjectMetrics

}
