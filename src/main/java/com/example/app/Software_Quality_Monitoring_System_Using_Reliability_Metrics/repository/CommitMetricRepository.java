package com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.repository;

import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity.CommitMetric;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommitMetricRepository extends JpaRepository<CommitMetric, Long> {
    // Пошук коміт-метрик по ID автора
    List<CommitMetric> findByAuthorId(Long authorId);

    // Метод для знаходження комітів за назвою проєкту
    List<CommitMetric> findByProjectName(String projectName);

    //Mетод для отримання комітів відсортованих за датою
    List<CommitMetric> findByProjectNameOrderByCommitDate(String projectName);

    List<CommitMetric> findAllByProjectName(String projectName);
    List<CommitMetric> findByProjectNameAndAuthor (String projectName, Employee author);
}
