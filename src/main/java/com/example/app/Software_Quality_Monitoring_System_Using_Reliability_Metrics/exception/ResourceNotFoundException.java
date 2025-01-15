package com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.exception;

/**
 Клас ResourceNotFoundException є власним винятком, який розширює RuntimeException. Його основне призначення — сигналізувати про помилку, коли запитуваний ресурс не знайдено в системі.

 Основні характеристики:
 Конструктор: Приймає повідомлення (типу String), яке передається у батьківський клас RuntimeException для збереження опису помилки.
 Призначення: Використовується для викидання винятку, коли ресурс (наприклад, запис у базі даних) не знайдений під час виконання програми.*/

public class ResourceNotFoundException extends RuntimeException{
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
