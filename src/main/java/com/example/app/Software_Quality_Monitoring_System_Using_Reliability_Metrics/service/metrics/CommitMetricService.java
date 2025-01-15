package com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.service.metrics;

import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity.CommitMetric;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity.DTO.CommitMetricDTO;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity.DTO.ProjectMetricsDTO;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.exception.ResourceNotFoundException;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.repository.CommitMetricRepository;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.repository.ProjectMetricsRepository;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.utilities.MetricConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Цей клас відповідає за обробку та аналіз метрик комітів і проектів, використовуючи дані з репозиторіїв.
 * <p>
 * getAllCommitMetrics():
 * <p>
 * Повертає список усіх метрик комітів з бази даних, перетворюючи їх у DTO (Data Transfer Objects) для подальшого використання.
 * getCommitMetricById(Long id):
 * <p>
 * Повертає одну метрику коміту за її ID у вигляді DTO, якщо така існує, або порожнє значення, якщо не знайдено.
 * getProjectMetricsById(Long id):
 * <p>
 * Повертає метрики проекту за його ID у вигляді DTO, або генерує виняток, якщо проект не знайдено в базі даних.
 * getAverageLinesAdded(String projectName):
 * <p>
 * Повертає середнє значення кількості доданих рядків для проекту за його назвою, або 0.0, якщо проект не знайдено.
 * getAverageLinesDeleted(String projectName):
 * <p>
 * Повертає середнє значення кількості видалених рядків для проекту за його назвою, або 0.0, якщо проект не знайдено.
 * Основний функціонал:
 * Отримання метрик: отримання метрик комітів і проектів через репозиторії.
 * Конвертація в DTO: використання конвертерів для перетворення сутностей у DTO.
 * Обчислення середніх значень: надання середніх значень для доданих і видалених рядків у проектах.
 */

@Service
public class CommitMetricService {
    private final CommitMetricRepository commitMetricRepository;
    private final ProjectMetricsRepository projectMetricsRepository;

    @Autowired
    public CommitMetricService(
            CommitMetricRepository commitMetricRepository,
            ProjectMetricsRepository projectMetricsRepository) {
        this.commitMetricRepository = commitMetricRepository;
        this.projectMetricsRepository = projectMetricsRepository;
    }

    // Отримати метрики для проекту за назвою
    public List<CommitMetric> getMetricsForProject(String projectName) {
        return commitMetricRepository.findByProjectName(projectName); // Використовуємо репозиторій для пошуку метрик
    }

    // Отримати всі коміт-метрики
    public List<CommitMetricDTO> getAllCommitMetrics() {
        return commitMetricRepository.findAll().stream()
                .map(MetricConverter::convertToDTO)
                .collect(Collectors.toList());
    }

    // Отримати коміт-метрику за ID
    public Optional<CommitMetricDTO> getCommitMetricById(Long id) {
        return commitMetricRepository.findById(id).map(MetricConverter::convertToDTO);
    }

    // Отримати метрики проекту за ID
    public ProjectMetricsDTO getProjectMetricsById(Long id) {
        return projectMetricsRepository.findById(id)
                .map(MetricConverter::convertToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Проект з ID " + id + " не знайдено."));
    }

}
