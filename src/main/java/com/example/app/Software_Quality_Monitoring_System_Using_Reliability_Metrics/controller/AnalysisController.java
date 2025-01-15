package com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.controller;

import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.service.analysis.AnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.DayOfWeek;
import java.util.Map;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    @Autowired
    private AnalysisService analysisService;

    // Перевірити наявність проекту
    @GetMapping("/check-project-existence")
    public ResponseEntity<Boolean> checkProjectExistence(@RequestParam String projectName) {
        return ResponseEntity.ok(analysisService.isProjectExists(projectName));
    }
    // Отримати середнє додано рядків для проекту
    @GetMapping("/average-lines-added")
    public ResponseEntity<String> getAverageLinesAdded(@RequestParam String projectName) {
        String averageLinesAdded = analysisService.getAverageLinesAdded(projectName);
        return ResponseEntity.ok(averageLinesAdded);
    }
    // Отримати середнє видалено рядків для проекту
    @GetMapping("/average-lines-deleted")
    public ResponseEntity<String> getAverageLinesDeleted(@RequestParam String projectName) {
        String averageLinesDeleted = analysisService.getAverageLinesDeleted(projectName);
        return ResponseEntity.ok(averageLinesDeleted);
    }
    // Отримати активність авторів для проекту
    @GetMapping("/author-activity")
    public ResponseEntity<Map<String, Long>> getAuthorActivity(@RequestParam String projectName) {
        return ResponseEntity.ok(analysisService.getAuthorActivity(projectName));
    }
    // Отримати середній час між комітами для проекту
    @GetMapping("/average-time-between-commits")
    public ResponseEntity<String> getAverageTimeBetweenCommits(@RequestParam String projectName) {
        String averageTimeBetweenCommits = analysisService.getAverageTimeBetweenCommits(projectName);
        return ResponseEntity.ok(averageTimeBetweenCommits);
    }
    // Отримати кількість комітів за днями тижня
    @GetMapping("/commit-count-by-day")
    public ResponseEntity<Map<DayOfWeek, Long>> getCommitCountByDayOfWeek(@RequestParam String projectName) {
        return ResponseEntity.ok(analysisService.getCommitCountByDayOfWeek(projectName));
    }
    // Отримати частоту типів файлів для проекту
    @GetMapping("/file-type-frequency")
    public ResponseEntity<Map<String, Long>> getFileTypeFrequency(@RequestParam String projectName) {
        return ResponseEntity.ok(analysisService.getFileTypeFrequency(projectName));
    }
}
