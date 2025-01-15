package com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.service.git.adapter;

import java.io.IOException;
import java.util.List;

/*Інтерфейс CommitAdapter описує методи для отримання інформації про коміт,
 який може бути адаптований для різних джерел комітів (наприклад,
  GitHub або локальний Git-репозиторій).

  Ось стислий опис кожного методу:
getSHA1()
Повертає хеш коміту у форматі SHA-1.

getAuthorName()
Повертає ім'я автора коміту. Викидає виключення IOException у разі помилок під час отримання даних.

getCommitterDate()
Повертає дату, коли коміт був зафіксований. Викидає виключення IOException.

getModifiedFiles()
Повертає список файлів, що були змінені в коміті. Викидає виключення IOException.

getFileType()
Повертає тип файлу на основі розширення (наприклад, "Java", "XML", "Text" тощо).

Цей інтерфейс дозволяє реалізовувати різні адаптери для роботи з комітами з
різних джерел (наприклад, GitHub, локальний Git) з однорідним API для
отримання даних про коміти.*/

public interface CommitAdapter {
    String getSHA1();
    String getAuthorName() throws IOException;
    java.util.Date getCommitterDate() throws IOException;
    List<String> getModifiedFiles() throws IOException;
    String getFileType();

}
