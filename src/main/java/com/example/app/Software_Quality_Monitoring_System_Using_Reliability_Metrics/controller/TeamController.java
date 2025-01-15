package com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.controller;

import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity.DTO.TeamDTO;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity.Employee;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity.Team;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.service.entity.TeamService;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.utilities.MetricConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teams")
public class TeamController {
    @Autowired
    private TeamService teamService;

    // Отримати команду за лідером
    @GetMapping("/team-lead/{teamLeadId}")
    public List<TeamDTO> getTeamsByTeamLeadId(@PathVariable Long teamLeadId) {
        List<Team> teams = teamService.getTeamsByTeamLeadId(teamLeadId);
        return teams.stream()
                .map(MetricConverter::convertToDTO)  // Використовуємо метод конвертації
                .collect(Collectors.toList());
    }

    // Отримати команду за проектом
    @GetMapping("/project/{projectName}")
    public List<TeamDTO> getTeamsByProjectName(@PathVariable String projectName) {
        List<Team> teams = teamService.getTeamsByProjectName(projectName);
        return teams.stream()
                .map(MetricConverter::convertToDTO)  // Використовуємо метод конвертації
                .collect(Collectors.toList());
    }
    // Створити працівника та додати його до проекту
    @PostMapping("/create-and-assign-to-project")
    public ResponseEntity<String> createEmployeeAndAssignToProject(
            @RequestParam String name,
            @RequestParam String projectName) {
        try {
            // Створюємо працівника та додаємо до команди
            Employee createdEmployee = teamService.createEmployeeAndAssignToProject(name, projectName);

            // Формуємо відповідь
            String responseMessage = "Працівника з ім'ям " + createdEmployee.getName() +
                    " успішно створено та додано до проекту '" + projectName + "'. ID: " +
                    createdEmployee.getId() + ", роль: " + createdEmployee.getRole() +
                    ", ставка: " + createdEmployee.getHourlyRate();
            return ResponseEntity.status(HttpStatus.CREATED).body(responseMessage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Не вдалося створити працівника та додати до проекту. Причина: " + e.getMessage());
        }
    }
}
