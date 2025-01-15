package com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.utilities;

import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity.CommitMetric;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity.DTO.CommitMetricDTO;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity.DTO.EmployeeDTO;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity.DTO.ProjectMetricsDTO;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity.DTO.TeamDTO;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity.Employee;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity.ProjectMetrics;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity.Team;

import java.util.stream.Collectors;

/**
 * Клас MetricConverter відповідає за перетворення об'єктів моделі у відповідні об'єкти DTO (Data Transfer Object), щоб передавати дані між шарами додатку (наприклад, з бази даних до користувацького інтерфейсу). Ось його функціонал:
 * <p>
 * convertToDTO(CommitMetric commitMetric):
 * <p>
 * Перетворює об'єкт CommitMetric в об'єкт CommitMetricDTO.
 * Копіює значення таких полів: commitHash, author, commitDate, linesAdded, linesDeleted.
 * convertToDTO(ProjectMetrics projectMetrics):
 * <p>
 * Перетворює об'єкт ProjectMetrics в об'єкт ProjectMetricsDTO.
 * Копіює значення полів: id, projectName, averageLinesAdded, averageLinesDeleted.
 * Перетворює список коміт-метрик (commitMetrics) у список відповідних DTO за допомогою методу convertToDTO для кожного елемента.
 * Цей клас використовує методи для трансформації даних у зручний формат для передачі, забезпечуючи правильне відображення даних із моделей до DTO.
 */

public class MetricConverter {
    public static CommitMetricDTO convertToDTO(CommitMetric commitMetric) {
        CommitMetricDTO dto = new CommitMetricDTO();
        dto.setCommitHash(commitMetric.getCommitHash());
        // Отримуємо ім'я автора з об'єкта Employee і передаємо як String
        String authorName = commitMetric.getAuthor() != null ? commitMetric.getAuthor().getName() : null;
        dto.setAuthor(authorName);  // Тепер передаємо лише ім'я автора
        dto.setProjectName(commitMetric.getProjectName());
        dto.setCommitDate(commitMetric.getCommitDate());
        dto.setLinesAdded(commitMetric.getLinesAdded());
        dto.setLinesDeleted(commitMetric.getLinesDeleted());
        dto.setFileType(commitMetric.getFileType());
        return dto;
    }

    public static ProjectMetricsDTO convertToDTO(ProjectMetrics projectMetrics) {
        ProjectMetricsDTO dto = new ProjectMetricsDTO();
        dto.setId(projectMetrics.getId());
        dto.setProjectName(projectMetrics.getProjectName());
        dto.setAverageLinesAdded(projectMetrics.getAverageLinesAdded());
        dto.setAverageLinesDeleted(projectMetrics.getAverageLinesDeleted());
        dto.setLastUpdated(projectMetrics.getLastUpdated());
        dto.setCommitMetrics(projectMetrics.getCommitMetrics().stream()
                .map(MetricConverter::convertToDTO)
                .collect(Collectors.toList()));
        return dto;
    }

    // Конвертація Employee в EmployeeDTO
    public static EmployeeDTO convertToDTO(Employee employee) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setId(employee.getId());
        dto.setName(employee.getName());
        dto.setHourlyRate(employee.getHourlyRate());
        dto.setRole(employee.getRole().toString());  // Перетворення Enum Role в String
        dto.setDateJoined(employee.getDateJoined().atStartOfDay().toLocalDate());
        return dto;
    }

    // Конвертація Team в TeamDTO
    public static TeamDTO convertToDTO(Team team) {
        TeamDTO dto = new TeamDTO();
        dto.setId(team.getId());
        dto.setProjectName(team.getProjectName());
        dto.setRepositoryPath(team.getRepositoryPath());

        // Перетворення teamLead в EmployeeDTO
        EmployeeDTO teamLeadDTO = convertToDTO(team.getTeamLead());
        dto.setTeamLead(teamLeadDTO);

        // Перетворення member в EmployeeDTO
        EmployeeDTO memberDTO = convertToDTO(team.getMember());
        dto.setMember(memberDTO);

        return dto;
    }
}
