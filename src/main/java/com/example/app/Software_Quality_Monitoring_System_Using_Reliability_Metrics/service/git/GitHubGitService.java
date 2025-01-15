package com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.service.git;

import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity.CommitMetric;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity.Employee;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity.ProjectMetrics;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity.Role;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.exception.RepositoryException;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.exception.ResourceNotFoundException;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.exception.SpecificException;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.repository.CommitMetricRepository;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.repository.EmployeeRepository;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.repository.GitRepositoryService;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.repository.ProjectMetricsRepository;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.service.git.adapter.CommitAdapter;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.service.git.adapter.GHCommitAdapter;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.utilities.GitUtils;
import jakarta.transaction.Transactional;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


/*Цей клас інтегрується з GitHub API для обробки комітів і збереження метрик проектів в базу даних.
Він реалізує інтерфейс GitService і використовує GitHub репозиторії для збору та обробки даних.

Ось опис функціоналу класу та основних методів:
1. saveCommitMetrics(String projectName, String repositoryPath, boolean isGitHubRepo)
Призначення: Зберігає метрики комітів для вказаного проекту з GitHub репозиторію.
Деталі:
Отримує список комітів з GitHub через getCommitsFromGitHub.
Визначає типи файлів і створює адаптери для кожного коміту.
Використовує метод GitUtils.saveCommitMetrics для збереження метрик.
Метод асинхронний, повертає CompletableFuture<Void>.
2. getCommitsFromGitHub(String repositoryPath)
Призначення: Отримує список комітів з GitHub репозиторію за заданим шляхом.
Деталі:
Використовує GitHub API для отримання репозиторію та його комітів.
Логування помилок, якщо коміти не вдалося отримати.
3. determineFileTypeFromGitHub(GHCommit commit)
Призначення: Визначає тип файлу на основі першого зміненого файлу в комітті.
Деталі:
Аналізує файли, змінені в комітті, та визначає тип на основі розширення файлу.
Повертає "Unknown", якщо файли не знайдені.
4. getCommitsForGitHubRepo(String owner, String repoName)
Призначення: Отримує список комітів для конкретного репозиторію на GitHub.
Деталі:
Запитує коміти з GitHub репозиторію за допомогою API.
Використовує GHRepository для доступу до списку комітів.
5. getOrCreateProjectMetrics(String projectName)
Призначення: Отримує або створює метрики проекту, якщо вони ще не існують.
Деталі:
Перевіряє, чи є в базі даних метрики для заданого проекту.
Якщо метрик немає, створює новий запис у базі даних для проекту.
6. updateProjectMetrics(String projectName)
Призначення: Оновлює метрики проекту в базі даних.
Деталі:
Обчислює середню кількість доданих та видалених рядків для проекту.
Оновлює відповідні поля в об'єкті ProjectMetrics.
7. getFilesFromRepo(String repositoryPath)
Призначення: Отримує список файлів з GitHub репозиторію.
Деталі:
Аналізує всі коміти репозиторію та збирає шляхи до змінених файлів.
Викидає помилки, якщо репозиторій не знайдений або є інші проблеми з доступом.
8. isRepositoryAlreadySaved(String projectName)
Призначення: Перевіряє, чи є збережені метрики для проекту в базі даних.
Деталі:
Використовує репозиторій ProjectMetricsRepository для перевірки існування проекту в базі даних.
9. saveCommitMetric(CommitAdapter adapter, String projectName)
Призначення: Зберігає метрику для одного коміту в базу даних.
Деталі:
Створює новий об'єкт CommitMetric на основі адаптера коміту.
Зберігає метрику коміту в базу даних.
Оновлює список комітів для проекту в базі даних.

    Основні функціональні моменти:
Інтеграція з GitHub API: Клас використовує GitHub для отримання інформації про коміти та файли з віддалених репозиторіїв.
Обробка помилок: Логування помилок через Logger замість e.printStackTrace().
Метрики комітів: Клас зберігає метрики комітів, такі як автор, дата, тип файлів, а також аналізує зміни в коді.
Базові операції з репозиторіями: Оновлення та створення записів у базі даних, перевірка існування проекту в базі, отримання метрик для проекту.*/


@Service
@Transactional
public class GitHubGitService implements GitRepositoryService {

    private final GitHub github;
    private final GitUtils gitUtils;
    private final ProjectMetricsRepository projectMetricsRepository;
    private final CommitMetricRepository commitMetricRepository;
    private final EmployeeRepository employeeRepository;
    private static final Logger logger = LoggerFactory.getLogger(GitHubGitService.class);

    @Autowired
    public GitHubGitService(@Value("${github.token}") String token,
                            GitUtils gitUtils,
                            CommitMetricRepository commitMetricRepository,
                            ProjectMetricsRepository projectMetricsRepository,
                            EmployeeRepository employeeRepository) throws IOException {

        this.github = new GitHubBuilder().withOAuthToken(token).build();
        this.gitUtils = gitUtils;
        this.projectMetricsRepository = projectMetricsRepository;
        this.commitMetricRepository = commitMetricRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public CompletableFuture<Void> saveCommitMetrics(String projectName, String repositoryPath,
                                                     boolean isGitHubRepo) throws IOException {
        List<CommitAdapter> commitAdapters = new ArrayList<>();
        try {
            // Отримуємо GitHub репозиторій
            GHRepository repository = github.getRepository(repositoryPath);

            // Отримуємо коміти з GitHub
            List<GHCommit> commits = getCommitsFromGitHub(repositoryPath);

            if (commits.isEmpty()) {
                logger.warn("У репозиторії {} немає комітів.", repositoryPath);

            }

            for (GHCommit commit : commits) {
                // Визначаємо тип файлів і створюємо адаптер
                String fileType = determineFileTypeFromGitHub(commit);
                GHCommitAdapter adapter = new GHCommitAdapter(commit, fileType, repository);
                commitAdapters.add(adapter);
            }

            if (commitAdapters.isEmpty()) {
                throw new RepositoryException("У репозиторії немає комітів.", repositoryPath);
            }

            // Виклик асинхронного методу з GitUtils
            return gitUtils.saveCommitMetricsAsync(commitAdapters, projectName, isGitHubRepo, projectMetricsRepository, commitMetricRepository);

        } catch (IOException e) {
            throw new RepositoryException("Помилка при збереженні метрик для GitHub репозиторію: " + repositoryPath, e);
        }
    }

    @Override
    public List<GHCommit> getCommitsForGitHubRepo(String projectName, String repositoryPath) throws IOException {
        return getCommitsFromGitHub(repositoryPath);
    }

    // Метод для отримання комітів з репозиторію GitHub
    private List<GHCommit> getCommitsFromGitHub(String repositoryPath) throws IOException {
        try {
            // Отримуємо репозиторій
            GHRepository repository = github.getRepository(repositoryPath);
            return repository.listCommits().toList();

        } catch (IOException e) {
            logger.error("Помилка при отриманні комітів з GitHub для репозиторію: {}", repositoryPath, e);
            throw new RepositoryException("Помилка при отриманні комітів з GitHub для репозиторію: " + repositoryPath, e);
        }
    }
    // Визначення типу файлів у комітті GitHub
    private String determineFileTypeFromGitHub(GHCommit commit) throws IOException {
        List<String> modifiedFiles = commit.getFiles().stream()
                .map(GHCommit.File::getFileName)
                .collect(Collectors.toList());

        if (modifiedFiles.isEmpty()) {
            logger.warn("У коміті {} немає змінених файлів.", commit.getSHA1());
        }

        return modifiedFiles.isEmpty() ? "Unknown" : GitUtils.determineFileType(modifiedFiles.get(0));
    }


    @Override
    public ProjectMetrics getOrCreateProjectMetrics(String projectName) {
        return projectMetricsRepository.findByProjectName(projectName).orElseGet(() -> projectMetricsRepository.save(new ProjectMetrics(projectName)));
    }

    @Override
    public void updateProjectMetrics(String projectName) throws IOException {
        ProjectMetrics projectMetrics = getOrCreateProjectMetrics(projectName);
        List<CommitMetric> metrics = commitMetricRepository.findByProjectName(projectName);
        if (!metrics.isEmpty()) {
            double averageAdded = metrics.stream().mapToDouble(CommitMetric::getLinesAdded).average().orElse(0.0);
            double averageDeleted = metrics.stream().mapToDouble(CommitMetric::getLinesDeleted).average().orElse(0.0);
            projectMetrics.setAverageLinesAdded(averageAdded);
            projectMetrics.setAverageLinesDeleted(averageDeleted);
            projectMetricsRepository.save(projectMetrics);
        } else {
            logger.warn("Не знайдено метрик для проєкту: {}", projectName);
        }
    }

    @Override
    public List<String> getFilesFromRepo(String repositoryPath) throws IOException {
        try {
            String[] parts = repositoryPath.split("/");
            if (parts.length < 2) {
                throw new SpecificException("Некоректний шлях до репозиторію: " + repositoryPath, 400);
            }

            GHRepository repository = github.getRepository(parts[0] + "/" + parts[1]);
            List<String> files = new ArrayList<>();

            // Перевірка комітів у репозиторії
            for (GHCommit commit : repository.listCommits()) {
                List<GHCommit.File> commitFiles = commit.getFiles();
                if (commitFiles.isEmpty()) {
                    logger.warn("Коміт {} не містить змінених файлів.", commit.getSHA1());
                    continue;  // Пропускаємо коміт без змін
                }

                // Додаємо лише змінені файли
                for (GHCommit.File file : commitFiles) {
                    files.add(file.getFileName());
                }
            }

            if (files.isEmpty()) {
                logger.info("У репозиторії {} немає змінених файлів.", repositoryPath);
            }

            return files;

        } catch (FileNotFoundException e) {
            logger.error("Репозиторій не знайдений: {}", repositoryPath);
            throw new ResourceNotFoundException("Репозиторій " + repositoryPath + " не знайдено.");
        } catch (IOException e) {
            logger.error("Помилка при доступі до репозиторію: {}", repositoryPath, e);
            throw new SpecificException("Помилка при доступі до GitHub для репозиторію " + repositoryPath, 500);
        }
    }

    // Метод для збереження метрик комітів
    private void saveCommitMetric(CommitAdapter adapter, String projectName) {
        CommitMetric metric = new CommitMetric();
        try {
            // Отримуємо проект з бази даних
            ProjectMetrics projectMetrics = projectMetricsRepository.findByProjectName(projectName)
                    .orElseGet(() -> {
                        logger.info("Проєкт {} не знайдений. Створюємо новий проект.", projectName);
                        return projectMetricsRepository.save(new ProjectMetrics(projectName));
                    });
            metric.setCommitHash(adapter.getSHA1());

            // Знаходимо автора у базі даних
            Employee author = employeeRepository.findByName(adapter.getAuthorName())
                    .orElseThrow(() -> {
                        try {
                            return new ResourceNotFoundException("Employee not found: " + adapter.getAuthorName());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
            metric.setAuthor(author);

            // Перетворюємо commitDate з Date в LocalDateTime
            LocalDateTime localDateTime = adapter.getCommitterDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

            metric.setCommitDate(localDateTime);
            metric.setFileType(adapter.getFileType());

            // Зберігаємо метрику коміту
            commitMetricRepository.save(metric);

            // Додаємо метрику в список комітів проекту
            projectMetrics.getCommitMetrics().add(metric);
            projectMetricsRepository.save(projectMetrics);

        } catch (IOException e) {
            logger.error("Помилка при збереженні метрики для коміту: ", e);
            // Обробка помилки
            e.printStackTrace();
        }
    }
    @Override
    public boolean isRepositoryAlreadySaved(String projectName) {
        return projectMetricsRepository.existsByProjectName(projectName);
    }

    public void addAuthorsToEmployeesFromGitHubRepository(String repositoryUrl, String gitHubToken) throws IOException {
        Set<String> authors = gitUtils.getAuthorsFromGitHubRepository(repositoryUrl, gitHubToken);

        for (String authorName : authors) {
            if (!employeeRepository.existsByName(authorName)) {
                Employee employee = new Employee();
                employee.setName(authorName);
                employee.setRole(Role.Developer);
                employee.setDateJoined(LocalDate.now());
                employeeRepository.save(employee);
            }
        }
    }

}
