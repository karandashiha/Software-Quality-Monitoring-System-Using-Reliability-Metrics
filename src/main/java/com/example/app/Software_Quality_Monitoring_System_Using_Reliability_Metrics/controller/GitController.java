package com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.controller;

import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.repository.GitRepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/git")
public class GitController {

    private static final Logger logger = LoggerFactory.getLogger(GitController.class);

    @Autowired
    private GitRepositoryService localGitService;
    @Autowired
    private GitRepositoryService gitHubGitService;

    // Уніфікований метод для вибору сервісу в залежності від типу репозиторію
    private GitRepositoryService getGitService(boolean isGitHubRepo) {
        return isGitHubRepo
                ? gitHubGitService : localGitService;
    }

    // Метод для збереження комітів
    @PostMapping("/save-commits")
    public ResponseEntity<String> saveCommits( @RequestParam String projectName,
                                               @RequestParam String repositoryPath,
                                               @RequestParam boolean isGitHubRepo) {
        try {
            GitRepositoryService gitRepositoryService = getGitService(isGitHubRepo);

            // Перевіряємо, чи репозиторій вже збережений
            if (gitRepositoryService.isRepositoryAlreadySaved(projectName)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Репозиторій вже збережено!");
            }
            // Зберігаємо коміт-метрики
            gitRepositoryService.saveCommitMetrics(projectName, repositoryPath, isGitHubRepo);
            return ResponseEntity.status(HttpStatus.CREATED).body("Дані збережено успішно.");
        } catch (Exception e) {
            logger.error("Помилка при збереженні репозиторію: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Сталася помилка: " + e.getMessage());
        }
    }

    // Перевірка наявності репозиторію (локальний чи GitHub)
    @GetMapping("/check/repository/{projectName}")
    public ResponseEntity<String> checkRepositoryExists(@PathVariable String projectName) {
        try {
            // Перевірка, чи існує репозиторій у локальному чи GitHub
            boolean exists = localGitService.isRepositoryAlreadySaved(projectName) ||
                    gitHubGitService.isRepositoryAlreadySaved(projectName);
            return exists
                    ? ResponseEntity.ok("Репозиторій існує.")
                    : ResponseEntity.status(HttpStatus.NOT_FOUND).body("Репозиторій не знайдено.");
        } catch (Exception e) {
            logger.error("Помилка при перевірці репозиторію: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
