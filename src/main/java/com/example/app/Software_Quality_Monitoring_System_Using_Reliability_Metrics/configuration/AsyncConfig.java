package com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**Цей клас конфігурує асинхронне виконання завдань у Spring додатку за допомогою ThreadPoolTaskExecutor.

 Основний компонент:

 ThreadPoolTaskExecutor — керує пулом потоків для асинхронного виконання задач.
 Налаштування:

 corePoolSize (10) — мінімальна кількість потоків у пулі.
 maxPoolSize (50) — максимальна кількість потоків у пулі.
 queueCapacity (100) — максимальна кількість задач, які можуть бути в черзі для виконання.
 threadNamePrefix ("async-exec-") — префікс для імен потоків.
 Анотація @EnableAsync: дозволяє асинхронне виконання методів у додатку.

 Метод taskExecutor(): створює та конфігурує екземпляр ThreadPoolTaskExecutor, який буде використовуватись для асинхронних операцій у додатку.*/

@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10); // Мінімальна кількість потоків
        executor.setMaxPoolSize(50);  // Максимальна кількість потоків
        executor.setQueueCapacity(100); // Кількість задач у черзі
        executor.setThreadNamePrefix("async-exec-");
        return executor;
    }
}
