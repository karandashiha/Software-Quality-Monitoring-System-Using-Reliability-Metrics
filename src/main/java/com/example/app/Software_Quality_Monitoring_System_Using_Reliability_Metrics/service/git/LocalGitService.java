package com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.service.git;

import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity.CommitMetric;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity.Employee;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity.ProjectMetrics;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity.Role;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.exception.RepositoryException;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.repository.CommitMetricRepository;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.repository.EmployeeRepository;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.repository.GitRepositoryService;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.repository.ProjectMetricsRepository;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.service.git.adapter.LocalCommitAdapter;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.utilities.GitUtils;
import jakarta.transaction.Transactional;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.kohsuke.github.GHCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * Клас LocalGitService реалізує інтерфейс GitService і відповідає за обробку локальних Git-репозиторіїв.
 * <p>
 * Функціонал:
 * <p>
 * 1. saveCommitMetrics(String projectName, String repositoryPath, boolean isGitHubRepo)
 * Призначення: Зберігає метрики комітів для локального репозиторію.
 * Деталі:
 * Відкриває репозиторій за вказаним шляхом.
 * Читає всі коміти з репозиторію.
 * Визначає типи файлів, що змінилися, та зберігає відповідні метрики для кожного коміту.
 * 2. getMostFrequentFileType(List<String> changedFiles)
 * Призначення: Визначає найбільш поширений тип файлів серед змінених у коміті.
 * Деталі:
 * Підраховує частоту кожного типу файлів і повертає найпоширеніший.
 * 3. saveCommitMetricsForProject(List<LocalCommitAdapter> commitAdapters, String projectName, boolean isGitHubRepo)
 * Призначення: Зберігає метрики комітів для конкретного проекту в базі даних.
 * Деталі:
 * Перевіряє наявність проекту в базі даних.
 * Зберігає метрики для кожного коміту.
 * Оновлює загальні метрики проекту, такі як середня кількість доданих та видалених рядків.
 * 4. updateProjectMetrics(ProjectMetrics projectMetrics)
 * Призначення: Оновлює метрики проекту після додавання нових комітів.
 * Деталі:
 * Обчислює середнє значення доданих і видалених рядків за всіма комітами проекту.
 * 5. getChangedFiles(RevCommit commit, Repository repository)
 * Призначення: Отримує список файлів, змінених в конкретному коміті.
 * Деталі:
 * Порівнює поточний коміт з батьківським комітом, щоб отримати змінені файли.
 * 6. getDiffs(RevCommit parentCommit, RevCommit commit, Repository repository)
 * Призначення: Отримує різницю (diff) між двома комітами.
 * Деталі:
 * Порівнює дерева файлів між двома комітами для отримання змін.
 * 7. getOrCreateProjectMetrics(String projectName)
 * Призначення: Отримує метрики проекту, або створює нові, якщо проект не знайдений.
 * Деталі:
 * Використовує репозиторій для пошуку метрик проекту в базі даних або створює нові.
 * 8. getFilesFromRepo(String repositoryPath)
 * Призначення: Отримує список змінених файлів з останнього коміту в локальному репозиторії.
 * Деталі:
 * Використовує Git API для отримання останнього коміту та переліку змінених файлів.
 * 9. isRepositoryAlreadySaved(String projectName)
 * Призначення: Перевіряє, чи існує вже проект з таким ім'ям в базі даних.
 * Деталі:
 * Використовує репозиторій ProjectMetricsRepository для перевірки наявності проекту.
 * Основні особливості:
 * Збереження метрик комітів: Збирає метрики для кожного коміту в локальному репозиторії.
 * Обчислення типів файлів: Визначає найбільш поширений тип файлів серед змінених у коміт.
 * Підтримка багатопоточності: Клас використовує асинхронні операції та виконує
 * збереження метрик без блокування головного потоку.
 */

@Service
@Transactional
public class LocalGitService implements GitRepositoryService {

    private final ProjectMetricsRepository projectMetricsRepository;
    private final CommitMetricRepository commitMetricRepository;
    private final GitUtils gitUtils;
    private final EmployeeRepository employeeRepository;

    private static final Logger logger = LoggerFactory.getLogger(LocalGitService.class);

    @Autowired
    public LocalGitService(
            GitUtils gitUtils,
            ProjectMetricsRepository projectMetricsRepository,
            CommitMetricRepository commitMetricRepository,
            EmployeeRepository employeeRepository) {

        this.gitUtils = gitUtils;
        this.projectMetricsRepository = projectMetricsRepository;
        this.commitMetricRepository = commitMetricRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public CompletableFuture<Void> saveCommitMetrics(String projectName, String repositoryPath, boolean isGitHubRepo) {
        File repoDir = new File(repositoryPath);
        if (!repoDir.exists() || !repoDir.isDirectory()) {
            throw new RepositoryException("Локальний репозиторій не знайдено за шляхом: " + repositoryPath, repositoryPath);
        }

        try (Git git = Git.open(repoDir)) {
            Repository repository = git.getRepository();

            // Отримуємо коміти з локального репозиторію
            List<LocalCommitAdapter> commitAdapters = new ArrayList<>();
            for (RevCommit commit : git.log().call()) {
                List<String> changedFiles = getChangedFiles(commit, repository);
                String mostFrequentFileType = getMostFrequentFileType(changedFiles);

                LocalCommitAdapter adapter = new LocalCommitAdapter(commit, repository, mostFrequentFileType, this);
                commitAdapters.add(adapter);
            }

            //Виклик асинхронного метод
            return gitUtils.saveCommitMetricsAsync(commitAdapters, projectName, isGitHubRepo, projectMetricsRepository, commitMetricRepository);

        } catch (IOException | GitAPIException e) {
            logger.error("Помилка при збереженні метрик для локального репозиторію: {}: {}", repositoryPath, e.getMessage());
            throw new RepositoryException("Помилка при збереженні метрик для локального репозиторію: " + repositoryPath, e, repositoryPath);
        }
    }

    public List<CommitMetric> getCommitMetrics(String projectName) {
        return commitMetricRepository.findAllByProjectName(projectName);
    }

    public Map<String, Long> getEmployeeContribution(String projectName, Employee author) {
        // Отримуємо список комітів
        List<CommitMetric> commitMetrics = commitMetricRepository.findByProjectNameAndAuthor(projectName, author);

        // Групуємо за типом файлів і рахуємо
        return commitMetrics.stream()
                .collect(Collectors.groupingBy(CommitMetric::getFileType, Collectors.counting()));
    }

    public ProjectMetrics getOrCreateProjectMetrics(String projectName) {
        return projectMetricsRepository.findByProjectName(projectName)
                .orElseGet(() -> projectMetricsRepository.save(new ProjectMetrics(projectName)));
    }

    private void updateProjectMetrics(ProjectMetrics projectMetrics) {
        double averageAdded = projectMetrics.getCommitMetrics().stream()
                .mapToDouble(CommitMetric::getLinesAdded)
                .average().orElse(0.0);
        double averageDeleted = projectMetrics.getCommitMetrics().stream()
                .mapToDouble(CommitMetric::getLinesDeleted)
                .average().orElse(0.0);

        projectMetrics.setAverageLinesAdded(averageAdded);
        projectMetrics.setAverageLinesDeleted(averageDeleted);
    }

    private String getMostFrequentFileType(List<String> changedFiles) {
        if (changedFiles.isEmpty()) {
            return "";
        }

        List<String> fileTypes = changedFiles.stream()
                .map(GitUtils::determineFileType)
                .toList();

        Map<String, Long> frequencyMap = fileTypes.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return frequencyMap.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("");
    }

    private List<String> getChangedFiles(RevCommit commit, Repository repository) throws IOException {
        if (commit.getParentCount() > 0) {
            try (ObjectReader reader = repository.newObjectReader();
                 RevWalk revWalk = new RevWalk(reader)) {
                RevCommit parent = commit.getParent(0);
                DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
                diffFormatter.setRepository(repository);
                List<DiffEntry> diffs = diffFormatter.scan(parent.getTree(), commit.getTree());

                return diffs.stream()
                        .map(DiffEntry::getNewPath)
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }

    public List<DiffEntry> getDiffs(RevCommit parentCommit, RevCommit commit, Repository repository) {
        try (ObjectReader reader = repository.newObjectReader()) {
            CanonicalTreeParser oldTreeParser = new CanonicalTreeParser();
            oldTreeParser.reset(reader, parentCommit.getTree().getId());
            CanonicalTreeParser newTreeParser = new CanonicalTreeParser();
            newTreeParser.reset(reader, commit.getTree().getId());
            try (Git git = new Git(repository)) {
                return git.diff()
                        .setOldTree(oldTreeParser)
                        .setNewTree(newTreeParser)
                        .call();
            }
        } catch (IOException | GitAPIException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public boolean isRepositoryAlreadySaved(String projectName) {
        return projectMetricsRepository.existsByProjectName(projectName);
    }

    @Override
    public void updateProjectMetrics(String projectName) {
        ProjectMetrics projectMetrics = getOrCreateProjectMetrics(projectName);
        updateProjectMetrics(projectMetrics);
        projectMetricsRepository.save(projectMetrics);
    }

    @Override
    public List<String> getFilesFromRepo(String repositoryPath) throws IOException {
        File repoDir = new File(repositoryPath);
        if (!repoDir.exists() || !repoDir.isDirectory()) {
            throw new RepositoryException("Локальний репозиторій не знайдено за шляхом: " + repositoryPath, repositoryPath);
        }
        try (Git git = Git.open(repoDir)) {
            Iterable<RevCommit> commits = git.log().setMaxCount(1).call();
            RevCommit latestCommit = commits.iterator().next();
            return getChangedFiles(latestCommit, git.getRepository());
        } catch (GitAPIException e) {
            throw new RepositoryException("Не вдалося отримати файли з локального репозиторію", repositoryPath);
        }
    }

    @Override
    public List<GHCommit> getCommitsForGitHubRepo(String projectName, String repositoryPath) {
        throw new UnsupportedOperationException("Цей метод не підтримується для локальних репозиторіїв.");
    }

    public void addAuthorsToEmployeesFromLocalRepository(String repositoryPath) throws IOException {
        Set<String> authors = gitUtils.getAuthorsFromLocalRepository(repositoryPath);

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
