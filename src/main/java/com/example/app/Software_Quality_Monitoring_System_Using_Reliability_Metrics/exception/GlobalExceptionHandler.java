package com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**Клас GlobalExceptionHandler обробляє різні типи винятків у Spring додатку за допомогою анотацій @ExceptionHandler. Ось стислий опис його функціоналу:

 Обробка загальних винятків:

 Метод handleAllExceptions обробляє будь-який загальний виняток (Exception).
 Повертає помилку з кодом 500 INTERNAL_SERVER_ERROR та деталями (час, повідомлення, опис запиту).
 Обробка специфічних винятків:

 Метод handleSpecificException обробляє виняток типу SpecificException.
 Повертає помилку з кодом 400 BAD_REQUEST і додатковими деталями (час, повідомлення, код помилки).
 Обробка помилок типу 404 (ресурс не знайдено):

 Метод handleResourceNotFoundException обробляє виняток ResourceNotFoundException.
 Повертає помилку з кодом 404 NOT_FOUND і повідомленням "Ресурс не знайдено".
 Обробка помилок репозиторію:

 Метод handleRepositoryException обробляє виняток RepositoryException.
 Повертає помилку з кодом 500 INTERNAL_SERVER_ERROR, а також логує повідомлення про помилку репозиторію в консолі.
 Цей клас дозволяє централізовано обробляти винятки та повертає користувачеві відповідні помилки з детальною інформацією.*/

@ControllerAdvice
public class GlobalExceptionHandler {

    // Обробка загальних винятків
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex, WebRequest request) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("message", ex.getMessage());
        errorDetails.put("details", request.getDescription(false));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDetails);
    }

    // Обробка специфічних винятків
    @ExceptionHandler(SpecificException.class)
    public ResponseEntity<Map<String, Object>> handleSpecificException(SpecificException ex, WebRequest request) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("message", ex.getCustomMessage());
        errorDetails.put("errorCode", ex.getErrorCode());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDetails);
    }

    // Обробка помилок типу 404 (не знайдено)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("message", ex.getMessage());
        errorDetails.put("details", "Ресурс не знайдено");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDetails);
    }
    @ExceptionHandler(RepositoryException.class)
    public ResponseEntity<String> handleRepositoryException(RepositoryException ex) {
        System.err.println("Помилка в репозиторії: " + ex.getMessage());
        return new ResponseEntity<>(
                "Помилка в репозиторії: " + ex.getMessage() + " (шлях: " + ex.getRepositoryPath() + ")",
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}