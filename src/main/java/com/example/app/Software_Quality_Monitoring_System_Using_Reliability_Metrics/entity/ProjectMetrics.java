package com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Клас ProjectMetrics є JPA-Entity, який представляє метрики проєкту в базі даних. Він містить наступний функціонал:
 * <p>
 * Атрибути:
 * <p>
 * id: Унікальний ідентифікатор проєкту.
 * projectName: Назва проєкту.
 * averageLinesAdded: Середня кількість рядків, доданих у проєкті.
 * averageLinesDeleted: Середня кількість рядків, видалених у проєкті.
 * lastUpdated: Дата та час останнього оновлення метрик проєкту.
 * Зв'язок з комітами:
 * <p>
 * Клас має зв'язок з CommitMetric через колекцію commitMetrics, що представляє всі коміти, пов'язані з цим проєктом.
 * Використовує анотацію @OneToMany для зв'язку з таблицею комітів.
 * Операції з комітами підтримують каскадне збереження та видалення (CascadeType.ALL), а також відключення сирітських об'єктів при видаленні.
 * Конструктори:
 * <p>
 * Конструктор для ініціалізації проєкту з назвою.
 * Пустий конструктор для роботи з JPA.
 * Цей клас використовує JPA для зберігання метрик проєкту та зв'язку з комітами в базі даних.
 */

@Entity
@Table(name = "project_metrics")
@Getter
@Setter
public class ProjectMetrics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_name")
    private String projectName;


    private double averageLinesAdded;
    private double averageLinesDeleted;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;


    @PreUpdate
    public void updateTimestamp() {
        this.lastUpdated = LocalDateTime.now();
    }

    // Зв'язок з комітами, які належать до даного проєкту
    @OneToMany(mappedBy = "projectMetrics", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<CommitMetric> commitMetrics = new ArrayList<>(); // Ініціалізація списку;

    // Додавання конструктора для ініціалізації з projectName
    public ProjectMetrics(String projectName) {
        this.projectName = projectName;
        this.commitMetrics = new ArrayList<>();
    }

    // Конструктор без параметрів для JPA
    public ProjectMetrics() {
    }
}