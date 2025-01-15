package com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.service.entity;

import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity.Employee;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity.ProjectMetrics;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity.Role;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity.Team;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.exception.ResourceNotFoundException;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.repository.CommitMetricRepository;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.repository.EmployeeRepository;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.repository.ProjectMetricsRepository;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.repository.TeamRepository;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.service.git.LocalGitService;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.service.git.adapter.CommitAdapter;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.utilities.EmployeeRoleAndRateAssigner;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.utilities.GitUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;


/**
 * Клас TeamService — це сервісний компонент у Java-програмі, який відповідає за бізнес-логіку, пов'язану з командами.
 * Основне завдання цього класу — забезпечити доступ до даних команд через методи, які взаємодіють із репозиторієм TeamRepository. Клас використовує анотацію @Service, яка визначає його як компонент Spring Framework, що забезпечує виконання бізнес-логіки.
 * <p>
 * Основні функції:
 * Отримання команд за ідентифікатором лідера команди:
 * <p>
 * Метод getTeamsByTeamLeadId(Long teamLeadId) повертає список команд, якими керує певний лідер.
 * Для цього використовується метод репозиторію findByTeamLeadId(teamLeadId).
 * Отримання команд за назвою проекту:
 * <p>
 * Метод getTeamsByProjectName(String projectName) повертає список команд, які працюють над певним проектом.
 * Для цього викликається метод репозиторію findByProjectName(projectName).
 */

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final EmployeeRepository employeeRepository;
    private final ProjectMetricsRepository projectMetricsRepository;
    private final CommitMetricRepository commitMetricRepository;
    private final GitUtils gitUtils;
    private final LocalGitService localGitService;
    private final EmployeeRoleAndRateAssigner roleAndRateAssigner;

    @Autowired
    public TeamService(TeamRepository teamRepository, EmployeeRepository employeeRepository,
                       ProjectMetricsRepository projectMetricsRepository, CommitMetricRepository commitMetricRepository,
                       GitUtils gitUtils, LocalGitService localGitService, EmployeeRoleAndRateAssigner roleAndRateAssigner) {

        this.teamRepository = teamRepository;
        this.employeeRepository = employeeRepository;
        this.projectMetricsRepository = projectMetricsRepository;
        this.commitMetricRepository = commitMetricRepository;
        this.gitUtils = gitUtils;
        this.localGitService = localGitService;
        this.roleAndRateAssigner = roleAndRateAssigner;
    }

    // Отримати команду за ID лідера
    public List<Team> getTeamsByTeamLeadId(Long teamLeadId) {
        return teamRepository.findByTeamLeadId(teamLeadId);
    }

    // Отримати команду за проектом
    public List<Team> getTeamsByProjectName(String projectName) {
        return teamRepository.findByProjectName(projectName);
    }

    //Метод для оновлення метрик проектів, прив’язаних до команди
    public void updateMetricsForTeamProjects(Long teamId) {
        // Отримуємо команду за ID
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Команду не знайдено"));

        // Отримуємо ім'я проекту через зв'язок з ProjectMetrics
        String projectName = team.getProjectMetrics().getProjectName();

        // Отримуємо шлях до репозиторію
        String repositoryPath = team.getRepositoryPath(); // додаємо поле repositoryPath у Team

        // Оновлюємо метрики для проекту
        localGitService.saveCommitMetrics(projectName, repositoryPath, false);
    }

    public Employee createEmployeeAndAssignToProject(String name, String projectName) {
        // Створюємо працівника
        Employee employee = new Employee();
        employee.setName(name);
        employee.setDateJoined(LocalDate.now());
        roleAndRateAssigner.assignRoleAndRate(employee, false);  // Призначаємо роль та ставку

        // Зберігаємо працівника в базі
        Employee savedEmployee = employeeRepository.save(employee);

        // Отримуємо шлях до репозиторію для проекту
        String repositoryPath = getRepositoryPathByProjectName(projectName);

        // Додаємо працівника до команди за проектом
        createOrUpdateTeam(savedEmployee.getId(), projectName, repositoryPath);

        return savedEmployee;
    }

    // Метод для отримання шляху до репозиторію з таблиці Team на основі назви проекту
    private String getRepositoryPathByProjectName(String projectName) {
        // Отримуємо першу команду для заданого проекту
        Team team = teamRepository.findByProjectName(projectName).stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Проект не знайдено"));
        // Повертаємо шлях до репозиторію
        return team.getRepositoryPath();
    }

    // Створення або оновлення команди
    public Team createOrUpdateTeam(Long memberId, String projectName, String repositoryPath) {
        // Завантаження учасника
        Employee member = employeeRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Учасника команди не знайдено"));

        // Завантаження метрик проекту
        ProjectMetrics projectMetrics = projectMetricsRepository.findByProjectName(projectName)
                .orElseGet(() -> {
                    ProjectMetrics newProjectMetrics = new ProjectMetrics(projectName);
                    return projectMetricsRepository.save(newProjectMetrics);
                });

        // Отримання існуючої команди проекту
        List<Team> existingTeams = teamRepository.findByProjectName(projectName);

        // Визначення лідера проекту
        Employee teamLead;
        if (!existingTeams.isEmpty()) {
            // Якщо лідер вже існує
            teamLead = existingTeams.get(0).getTeamLead();
        } else {
            // Якщо це новий проект, встановлюємо учасника як лідера
            teamLead = member;
        }

        // Перевірка наявності учасника у команді
        boolean memberExists = existingTeams.stream()
                .anyMatch(team -> team.getMember().getId().equals(memberId));

        if (!memberExists) {
            // Створення нового запису для учасника
            Team newTeam = new Team();
            newTeam.setMember(member);
            newTeam.setTeamLead(teamLead);
            newTeam.setProjectName(projectName);
            newTeam.setRepositoryPath(repositoryPath);
            newTeam.setProjectMetrics(projectMetrics);

            // Збереження команди
            return teamRepository.save(newTeam);
        } else {
            // Якщо учасник вже існує, повертаємо існуючий запис
            return existingTeams.stream()
                    .filter(team -> team.getMember().getId().equals(memberId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Помилка оновлення команди"));
        }
    }

    // Обробка метрик комітів через утилітний клас
    public void processCommitMetrics(List<? extends CommitAdapter> commitAdapters,
                                     String projectName,
                                     boolean isGitHubRepo) {
        try {
            // Виклик методу saveCommitMetrics з GitUtils
            new GitUtils(
                    projectMetricsRepository,
                    commitMetricRepository,
                    teamRepository,
                    employeeRepository
            ).saveCommitMetrics(
                    commitAdapters,
                    projectName,
                    isGitHubRepo,
                    projectMetricsRepository,
                    commitMetricRepository
            );
        } catch (Exception e) {
            throw new RuntimeException("Помилка при обробці метрик комітів для проекту: " + projectName, e);
        }
    }

    // Автоматичне створення команд з репозиторію
    public void createTeamsFromRepository(String repositoryPath, String projectName,
                                          String repositoryUrl, String gitHubToken,
                                          boolean isGitHubRepo) throws IOException {

        Set<String> authors = isGitHubRepo
                ? gitUtils.getAuthorsFromGitHubRepository(repositoryUrl, gitHubToken)
                : gitUtils.getAuthorsFromLocalRepository(repositoryPath);

        Employee firstAuthor = null;

        for (String authorName : authors) {
            Employee author = employeeRepository.findByName(authorName)
                    .orElseGet(() -> {
                        Employee newAuthor = new Employee();
                        newAuthor.setName(authorName);
                        newAuthor.setRole(Role.Developer);
                        newAuthor.setDateJoined(LocalDate.now());
                        return employeeRepository.save(newAuthor);
                    });

            if (firstAuthor == null) {
                firstAuthor = author;
            }

            createOrUpdateTeam(firstAuthor.getId(), projectName, repositoryPath);
        }
    }
}
