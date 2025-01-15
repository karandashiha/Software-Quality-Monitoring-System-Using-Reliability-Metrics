package com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity.DTO;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**Опис класу ProjectMetricsDTO:
 Цей клас є Data Transfer Object (DTO) для зберігання метрик проекту. Він призначений для передачі інформації про проект та його коміт-метрики між шарами програми або зовнішніми сервісами.

 Поля:

 id: Унікальний ідентифікатор проекту.
 projectName: Назва проекту.
 averageLinesAdded: Середнє значення доданих рядків у комітах.
 averageLinesDeleted: Середнє значення видалених рядків у комітах.
 lastUpdated: Дата та час останнього оновлення метрик проекту.
 commitMetrics: Список метрик комітів для цього проекту (представлених об'єктами CommitMetricDTO).
 Призначення:

 Забезпечує структуру для збереження та передачі статистики по проекту, що включає інформацію про середню кількість змінених рядків (доданих та видалених) та деталі кожного коміту через інші DTO (наприклад, CommitMetricDTO).*/

@Getter
@Setter
public class ProjectMetricsDTO {
    private Long id;
    private String projectName;
    private double averageLinesAdded;
    private double averageLinesDeleted;
    private LocalDateTime lastUpdated;
    private List<CommitMetricDTO> commitMetrics;
}
