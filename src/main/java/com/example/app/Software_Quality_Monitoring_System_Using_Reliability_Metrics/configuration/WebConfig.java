package com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.configuration;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**Клас WebConfig реалізує інтерфейс WebMvcConfigurer і налаштовує конфігурацію CORS (Cross-Origin Resource Sharing) для веб-додатку.

 Основний функціонал:
 Налаштування CORS:
 Дозволяє запити з певного джерела (http://localhost:63342).
 Дозволяє запити для всіх ендпоінтів (/**).
 Дозволяє використання таких HTTP методів: GET, POST, PUT, DELETE, OPTIONS.
 Дозволяє всі заголовки (*).
 Цей клас дозволяє контролювати доступ з різних доменів і налаштовувати правила для запитів, що надходять від клієнтів.*/

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Дозволити запити на всі ендпоінти
                .allowedOrigins("http://localhost:63342") // Дозволити запити тільки з цього походження
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Дозволені HTTP методи
                .allowedHeaders("*"); // Дозволити всі заголовки
    }
}