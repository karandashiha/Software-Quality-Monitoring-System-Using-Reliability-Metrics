package com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.exception;

/**
 Клас RepositoryException є розширенням стандартного виключення RuntimeException і використовується для обробки помилок, що виникають під час роботи з репозиторіями.

 Основні функціональні можливості:
 Конструктори:

 RepositoryException(String message, String repositoryPath): Приймає повідомлення про помилку та шлях до репозиторію.
 RepositoryException(String message, Throwable cause, String repositoryPath): Приймає повідомлення про помилку, причину (інше виключення) та шлях до репозиторію.
 RepositoryException(String message, Throwable cause): Приймає повідомлення про помилку та причину, без збереження шляху репозиторію.
 getRepositoryPath(): Метод для отримання шляху до репозиторію, що зберігається в екземплярі класу (якщо він наданий).

 Призначення:
 Обробка специфічних помилок під час роботи з репозиторіями (наприклад, при взаємодії з файловою системою чи Git-репозиторієм).
 Клас дозволяє зберігати інформацію про шлях до репозиторію для більш детальної інформації про помилку.*/

public class RepositoryException extends RuntimeException {
    private final String repositoryPath;

    public RepositoryException(String message, String repositoryPath) {
        super(message);
        this.repositoryPath = repositoryPath;
    }

    public RepositoryException(String message, Throwable cause, String repositoryPath) {
        super(message, cause);
        this.repositoryPath = repositoryPath;
    }
    // Доданий конструктор, який дозволяє передавати IOException без шляху
    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
        this.repositoryPath = null;  // або можна зберігати шляхи, якщо потрібно
    }
    public String getRepositoryPath() {
        return repositoryPath;
    }
}
