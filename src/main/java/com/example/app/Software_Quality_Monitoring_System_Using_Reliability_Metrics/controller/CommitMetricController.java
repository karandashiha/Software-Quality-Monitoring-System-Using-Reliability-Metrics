package com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.controller;

import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity.CommitMetric;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity.DTO.CommitMetricDTO;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity.DTO.ProjectMetricsDTO;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.service.metrics.CommitMetricService;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.utilities.MetricConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/metrics")
public class CommitMetricController {

    @Autowired
    private CommitMetricService commitMetricService;

    @GetMapping
    public ResponseEntity<List<CommitMetricDTO>> getAllMetrics() {
        return ResponseEntity.ok(commitMetricService.getAllCommitMetrics());
    }
    // Отримати метрики проекту за назвою
    @GetMapping("/project")
    public ResponseEntity<List<CommitMetricDTO>> getMetricsForProject(@RequestParam String projectName) {
        List<CommitMetric> metrics = commitMetricService.getMetricsForProject(projectName);
        if (metrics.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } else {
            List<CommitMetricDTO> metricsDTO = metrics.stream()
                    .map(MetricConverter::convertToDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(metricsDTO);
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<CommitMetricDTO> getCommitMetricById(@PathVariable Long id) {
        return commitMetricService.getCommitMetricById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
    // Отримати метрики проекту за ID
    @GetMapping("/project/{projectId}")
    public ResponseEntity<ProjectMetricsDTO> getProjectMetrics(@PathVariable Long projectId) {
        ProjectMetricsDTO dto = commitMetricService.getProjectMetricsById(projectId);
        return dto != null
                ? ResponseEntity.ok(dto)
                : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
