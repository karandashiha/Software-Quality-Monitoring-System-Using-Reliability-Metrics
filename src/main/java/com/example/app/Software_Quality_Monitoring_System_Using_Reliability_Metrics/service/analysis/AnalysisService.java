package com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.service.analysis;

import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity.CommitMetric;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity.ProjectMetrics;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.exception.ResourceNotFoundException;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.repository.CommitMetricRepository;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.repository.ProjectMetricsRepository;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.service.git.LocalGitService;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.utilities.Rounder;
import jakarta.transaction.Transactional;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**AnalysisService — це сервісний клас, що відповідає за аналіз та статистику даних комітів для певного проекту в базі даних. Він працює з репозиторіями CommitMetricRepository та ProjectMetricsRepository, щоб виконувати різні операції, пов'язані з метриками комітів і проектів.

 Основні методи:
 findProjectMetricsOrThrow(String projectName)

 Призначення: Знаходить метрики проекту за його назвою, або викидає виняток, якщо проект не знайдено.
 isProjectExists(String projectName)

 Призначення: Перевіряє, чи існують метрики для проекту з заданою назвою.
 getAverageLinesAdded(String projectName)

 Призначення: Повертає середню кількість доданих рядків для заданого проекту.
 getAverageLinesDeleted(String projectName)

 Призначення: Повертає середню кількість видалених рядків для заданого проекту.
 getAuthorActivity(String projectName)

 Призначення: Повертає статистику активності авторів комітів (кількість комітів кожного автора) для проекту.
 getAverageTimeBetweenCommits(String projectName)

 Призначення: Обчислює середній час між комітами для проекту в годинах.
 getCommitCountByDayOfWeek(String projectName)

 Призначення: Повертає кількість комітів за кожен день тижня для заданого проекту.
 getFileTypeFrequency(String projectName)

 Призначення: Повертає статистику по типах файлів, які змінювалися в комітах, для проекту (наприклад, скільки разів було змінено .java, .xml файли тощо).
 validateProjectExists(String projectName)

 Призначення: Перевіряє наявність проекту перед виконанням операцій, які потребують наявності метрик цього проекту.
 Підсумок:
 Цей клас надає методи для збору різних аналітичних даних з комітів: середнє значення рядків, змінених в комітах, активність авторів, середній час між комітами, статистика за днями тижня та за типами файлів. Всі методи забезпечують перевірку існування проекту та обробку виключень.*/

@Service
@Transactional
public class AnalysisService {
    private final CommitMetricRepository commitMetricRepository;
    private final ProjectMetricsRepository projectMetricsRepository;
    private final LocalGitService localGitService;

    @Autowired
    public AnalysisService(
            CommitMetricRepository commitMetricRepository,
            ProjectMetricsRepository projectMetricsRepository,
            LocalGitService localGitService) {
        this.commitMetricRepository = commitMetricRepository;
        this.projectMetricsRepository = projectMetricsRepository;
        this.localGitService = localGitService;
    }

    public void analyzeLocalRepo(String projectName, String repositoryPath) throws IOException, GitAPIException {
        localGitService.saveCommitMetrics(projectName, repositoryPath, false);
        // Аналізуйте отримані дані
    }


    private ProjectMetrics findProjectMetricsOrThrow(String projectName) {
        return projectMetricsRepository.findByProjectName(projectName)
                .orElseThrow(() -> new ResourceNotFoundException("Проект з назвою " + projectName + " не знайдено."));
    }

    public boolean isProjectExists(String projectName) {
        return projectMetricsRepository.existsByProjectName(projectName);
    }

    // Отримати середнє додано рядків для проекту
    public String getAverageLinesAdded(String projectName) {
        double average = projectMetricsRepository.findByProjectName(projectName)
                .map(ProjectMetrics::getAverageLinesAdded)
                .orElse(0.0);
        return Rounder.roundValue(average);
    }

    // Отримати середнє видалено рядків для проекту
    public String getAverageLinesDeleted(String projectName) {
        double average = projectMetricsRepository.findByProjectName(projectName)
                .map(ProjectMetrics::getAverageLinesDeleted)
                .orElse(0.0);
        return Rounder.roundValue(average);
    }

    public Map<String, Long> getAuthorActivity(String projectName) {
        validateProjectExists(projectName);
        return commitMetricRepository.findByProjectName(projectName).stream()
                .collect(Collectors.groupingBy(metric -> metric.getAuthor().getName(), Collectors.counting()));

    }

public String getAverageTimeBetweenCommits(String projectName) {
    List<CommitMetric> metrics = commitMetricRepository.findByProjectNameOrderByCommitDate(projectName);
    if (metrics.size() < 2) return Rounder.roundValue(0.0);

    double totalHours = IntStream.range(1, metrics.size())
            .mapToDouble(i -> Duration.between(metrics.get(i - 1).getCommitDate(), metrics.get(i).getCommitDate())
                    .toHours())
            .sum();

    double average = totalHours / (metrics.size() - 1);
    return Rounder.roundValue(average);
}


    public Map<DayOfWeek, Long> getCommitCountByDayOfWeek(String projectName) {
        validateProjectExists(projectName);
        return commitMetricRepository.findByProjectName(projectName).stream()
                .collect(Collectors.groupingBy(metric -> metric.getCommitDate().getDayOfWeek(), Collectors.counting()));
    }

    public Map<String, Long> getFileTypeFrequency(String projectName) {
        validateProjectExists(projectName);
        return commitMetricRepository.findByProjectName(projectName).stream()
                .filter(metric -> metric.getFileType() != null && !metric.getFileType().isEmpty())
                .collect(Collectors.groupingBy(CommitMetric::getFileType, Collectors.counting()));
    }

    private void validateProjectExists(String projectName) {
        if (!isProjectExists(projectName)) {
            throw new ResourceNotFoundException("Проект з назвою " + projectName + " не знайдено.");
        }
    }
}
