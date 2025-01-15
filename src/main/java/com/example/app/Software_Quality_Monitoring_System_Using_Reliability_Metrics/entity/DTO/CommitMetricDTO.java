package com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity.DTO;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**Опис класу CommitMetricDTO
 Клас CommitMetricDTO є Data Transfer Object (DTO), який використовується для передачі інформації про метрики коміту між різними компонентами програми (наприклад, між сервісами або між сервісом і користувачем).

 Поля:

 commitHash: хеш коміту.
 author: автор коміту.
 commitDate: дата і час коміту.
 linesAdded: кількість доданих рядків.
 linesDeleted: кількість видалених рядків.
 Використовуються стандартні геттери та сеттери для доступу та зміни значень полів.

 Цей клас, призначений для передачі зібраної інформації про коміти в інтерфейсі користувача або для збереження в іншій системі.*/

@Getter
@Setter
public class CommitMetricDTO {
    private String commitHash;
    private String author;
    private String projectName;
    private LocalDateTime commitDate;
    private int linesAdded;
    private int linesDeleted;
    private String fileType;

}
