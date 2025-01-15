package com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.repository;

import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity.ProjectMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**Опис класу ProjectMetricsRepository:
 Цей інтерфейс є частиною механізму доступу до даних і використовує JpaRepository для роботи з об'єктами ProjectMetrics в базі даних.

 findByProjectName(String projectName):

 Шукає проект у базі даних за його назвою.
 Повертає об'єкт Optional<ProjectMetrics>, що містить метрики проекту, якщо вони є, або порожній результат, якщо такого проекту немає.
 existsByProjectName(String projectName):

 Перевіряє, чи існує проект з заданою назвою в базі даних.
 Повертає true, якщо проект знайдений, і false в іншому випад*/
@Repository
public interface ProjectMetricsRepository extends JpaRepository<ProjectMetrics, Long> {
    Optional<ProjectMetrics> findByProjectName(String projectName);
    boolean existsByProjectName(String projectName);

}
