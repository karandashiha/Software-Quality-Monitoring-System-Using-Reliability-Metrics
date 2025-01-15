package com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.utilities;

import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity.CommitMetric;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity.Employee;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity.ProjectMetrics;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity.Team;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.repository.CommitMetricRepository;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.repository.EmployeeRepository;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.repository.ProjectMetricsRepository;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.repository.TeamRepository;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.service.git.adapter.CommitAdapter;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.service.git.adapter.GHCommitAdapter;
import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.service.git.adapter.LocalCommitAdapter;
import jakarta.transaction.Transactional;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.kohsuke.github.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Клас GitUtils містить допоміжні методи для роботи з Git-репозиторіями та обробки метрик комітів. Ось короткий опис основних методів цього класу:
 * <p>
 * 1. saveCommitMetrics(List<? extends CommitAdapter> commitAdapters, String projectName, boolean isGitHubRepo, ProjectMetricsRepository projectMetricsRepository, CommitMetricRepository commitMetricRepository)
 * Призначення: Асинхронно зберігає метрики комітів для зазначеного проекту.
 * Деталі:
 * Створює або знаходить метрики проекту.
 * Обробляє список адаптерів комітів, зберігаючи відповідні метрики в базу даних.
 * Підраховує змінені рядки для кожного коміту через специфічні адаптери (локальний або GitHub).
 * 2. getTreeParser(Repository repository, ObjectId objectId)
 * Призначення: Повертає парсер дерева для заданого ObjectId.
 * Деталі:
 * Використовує ObjectReader для створення парсера дерева з репозиторію Git.
 * 3. determineFileType(String fileName)
 * Призначення: Визначає тип файлу на основі його розширення.
 * Деталі:
 * Підтримує різні типи файлів (наприклад, Java, HTML, CSS, Python тощо).
 * 4. getFileExtension(String fileName)
 * Призначення: Повертає розширення файлу.
 * Деталі:
 * Визначає розширення файлу на основі його імені.
 * 5. getModifiedFiles(RevCommit commit, Repository repository)
 * Призначення: Повертає список змінених файлів у коміті.
 * Деталі:
 * Порівнює старе та нове дерево файлів, щоб знайти змінені файли.
 * 6. countLines(Repository repository, ObjectId objectId)
 * Призначення: Підраховує кількість рядків у файлі, зазначеному через ObjectId.
 * Деталі:
 * Читає файл з репозиторію та рахує кількість рядків.
 * 7. calculateLinesChanged(RevCommit commit, CommitMetric metric, Repository repository)
 * Призначення: Підраховує кількість доданих і видалених рядків між двома комітами.
 * Деталі:
 * Порівнює змінені файли між батьківським та поточним комітами, підраховуючи додані та видалені рядки.
 * 8. updateProjectMetrics(ProjectMetrics projectMetrics, CommitMetricRepository commitMetricRepository)
 * Призначення: Оновлює метрики проекту, розраховуючи середні значення доданих і видалених рядків.
 * Деталі:
 * Підраховує середнє значення доданих і видалених рядків для комітів проекту.
 * Основні покращення та особливості:
 * Асинхронне збереження метрик за допомогою CompletableFuture.
 * Логіка визначення типу файлів через картку розширень.
 * Обробка змін у файлах за допомогою DiffFormatter.
 * Підтримка роботи з різними типами репозиторіїв, включаючи локальні і GitHub.
 * Підрахунок кількості змін у файлах між комітами через порівняння дерев змін.
 */

@Service
@Transactional
public class GitUtils {

    @Autowired
    private EmployeeRoleAndRateAssigner roleAndRateAssigner;

    private final ProjectMetricsRepository projectMetricsRepository;
    private final CommitMetricRepository commitMetricRepository;
    private final TeamRepository teamRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public GitUtils(ProjectMetricsRepository projectMetricsRepository,
                    CommitMetricRepository commitMetricRepository,
                    TeamRepository teamRepository,
                    EmployeeRepository employeeRepository) {
        this.projectMetricsRepository = projectMetricsRepository;
        this.commitMetricRepository = commitMetricRepository;
        this.teamRepository = teamRepository;
        this.employeeRepository = employeeRepository;
    }

    private static final Logger logger = LoggerFactory.getLogger(GitUtils.class);


    public CompletableFuture<Void> saveCommitMetricsAsync(
            List<? extends CommitAdapter> commitAdapters,
            String projectName,
            boolean isGitHubRepo,
            ProjectMetricsRepository projectMetricsRepository,
            CommitMetricRepository commitMetricRepository) {
        return CompletableFuture.runAsync(() -> saveCommitMetrics(
                commitAdapters, projectName, isGitHubRepo, projectMetricsRepository, commitMetricRepository));
    }

    public void saveCommitMetrics(
            List<? extends CommitAdapter> commitAdapters,
            String projectName,
            boolean isGitHubRepo,
            ProjectMetricsRepository projectMetricsRepository,
            CommitMetricRepository commitMetricRepository) {

        // Отримуємо або створюємо метрики проекту
        ProjectMetrics projectMetrics = projectMetricsRepository.findByProjectName(projectName)
                .orElseGet(() -> {
                    ProjectMetrics newProjectMetrics = new ProjectMetrics(projectName);
                    return projectMetricsRepository.save(newProjectMetrics);
                });

        // Використовуємо AtomicReference для збереження проектного лідера
        final AtomicReference<Employee> projectLead = new AtomicReference<>(null);

        for (CommitAdapter adapter : commitAdapters) {
            CommitMetric commitMetric = new CommitMetric();
            try {
                commitMetric.setCommitHash(adapter.getSHA1());

                // Перевіряємо, чи є автор у базі, і створюємо його, якщо потрібно
                Employee author = employeeRepository.findByName(adapter.getAuthorName())
                        .orElseGet(() -> {
                            Employee newAuthor = new Employee();
                            try {
                                newAuthor.setName(adapter.getAuthorName());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            // Викликаємо утилітний клас для визначення ролі та ставки
                            roleAndRateAssigner.assignRoleAndRate(newAuthor, projectLead.get() == null);
                            newAuthor.setDateJoined(LocalDate.now()); // Встановлюємо дату приєднання
                            return employeeRepository.save(newAuthor);
                        });

                commitMetric.setAuthor(author);
                commitMetric.setCommitDate(LocalDateTime.ofInstant(adapter.getCommitterDate().toInstant(), ZoneId.systemDefault()));
                commitMetric.setFileType(adapter.getFileType());
                commitMetric.setProjectName(projectName);
                commitMetric.setProjectMetrics(projectMetrics);

                // Логіка для локальних і GitHub адаптерів
                if (adapter instanceof GHCommitAdapter) {
                    calculateLinesChangedForGitHub((GHCommitAdapter) adapter, commitMetric);
                } else if (adapter instanceof LocalCommitAdapter) {
                    LocalCommitAdapter localAdapter = (LocalCommitAdapter) adapter;
                    try {
                        calculateLinesChangedForLocal(localAdapter.getCommit(), commitMetric, localAdapter.getRepository());
                    } catch (IOException e) {
                        logger.warn("Помилка обчислення змінених рядків для коміту {}: {}", adapter.getSHA1(), e.getMessage());
                        throw new RuntimeException(e);
                    }
                } else {
                    commitMetric.setLinesAdded(0);
                    commitMetric.setLinesDeleted(0);
                }
                commitMetricRepository.save(commitMetric);
                projectMetrics.getCommitMetrics().add(commitMetric);

                // Додавання або оновлення запису в таблиці команд
                Team existingTeam = teamRepository.findByProjectNameAndMemberId(projectName, author.getId())
                        .orElseGet(() -> {
                            Team newTeam = new Team();
                            newTeam.setMember(author);
                            newTeam.setProjectName(projectName);
                            newTeam.setProjectMetrics(projectMetrics);

                            // Встановлення шляху до репозиторію
                            String repositoryPath = null;
                            try {
                                repositoryPath = getRepositoryPath(adapter);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            newTeam.setRepositoryPath(repositoryPath);

                            // Призначаємо лідера для проекту, якщо він ще не встановлений
                            if (projectLead.get() == null) {
                                projectLead.set(author); // Призначаємо першого автора лідером
                                newTeam.setTeamLead(author);
                                roleAndRateAssigner.assignRoleAndRate(author, true);
                            } else {
                                newTeam.setTeamLead(projectLead.get()); // Встановлюємо вже існуючого лідера
                                roleAndRateAssigner.assignRoleAndRate(author, false);
                            }

                            return teamRepository.save(newTeam);
                        });

            } catch (Exception e) {
                logger.error("Помилка при обробці метрик коміту: " + adapter.getSHA1(), e);
            }
        }

        // Оновлюємо метрики проекту
        updateProjectMetrics(projectMetrics);
        projectMetricsRepository.save(projectMetrics);
    }

    public static AbstractTreeIterator getTreeParser(Repository repository, ObjectId objectId) throws IOException {
        try (ObjectReader reader = repository.newObjectReader()) {
            CanonicalTreeParser treeParser = new CanonicalTreeParser();
            treeParser.reset(reader, objectId);  // Підготовка парсера для переданого об'єкта дерева
            return treeParser;
        }
    }


    public static String determineFileType(String fileName) {
        String extension = getFileExtension(fileName);
        switch (extension) {
            case "java":
                return "Java";
            case "xml":
                return "XML";
            case "jenkinsfile":
            case "txt":
                return "Text";
            case "adoc":
                return "AsciiDoc";
            case "doc":
            case "docx":
                return "Word Document";
            case "html":
                return "HTML";
            case "css":
                return "CSS";
            case "py":
                return "Python Script";
            case "js":
                return "JavaScript";
            case "php":
                return "PHP";
            case "json":
                return "JSON";
            case "md":
                return "Markdown";
            case "gradle":
                return "Gradle";
            case "jar":
                return "JAR";
            case "png":
                return "PNG Image";
            case "sh":
                return "Shell";
            case "cpp":
                return "C++";
            case "bat":
                return "Batch File";
            case "properties":
                return "Properties";

            default:
                return "Unknown";
        }
    }

    public static String getFileExtension(String fileName) {
        int lastIndex = fileName.lastIndexOf('.');
        return (lastIndex == -1) ? "" : fileName.substring(lastIndex + 1);
    }

    public static List<String> getModifiedFiles(RevCommit commit, Repository repository) throws IOException {
        List<String> modifiedFiles = new ArrayList<>();
        ObjectId oldTreeId = commit.getParentCount() > 0
                ? commit.getParent(0).getTree().getId()
                : null;
        ObjectId newTreeId = commit.getTree().getId();

        try (ObjectReader reader = repository.newObjectReader()) {
            AbstractTreeIterator oldTreeParser = oldTreeId != null ? getTreeParser(repository, oldTreeId) : null;
            AbstractTreeIterator newTreeParser = getTreeParser(repository, newTreeId);

            try (DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
                diffFormatter.setRepository(repository);
                List<DiffEntry> entries = diffFormatter.scan(oldTreeParser, newTreeParser);
                for (DiffEntry entry : entries) {
                    modifiedFiles.add(entry.getNewPath());
                }
            }
        }
        return modifiedFiles;
    }

    public static int countLines(Repository repository, ObjectId objectId) throws IOException {
        try (ObjectReader reader = repository.newObjectReader()) {
            ObjectLoader loader = reader.open(objectId);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(loader.openStream()));
            int lines = 0;
            while (bufferedReader.readLine() != null) {
                lines++;
            }
            return lines;
        }
    }

    // Калькуляція змін для GitHub комітів
    private void calculateLinesChangedForGitHub(GHCommitAdapter adapter, CommitMetric metric) throws IOException {
        int linesAdded = 0, linesDeleted = 0;

        // Перебираємо файли в коміті та обчислюємо кількість доданих і видалених рядків
        for (GHCommit.File file : adapter.getGhCommit().getFiles()) {
            linesAdded += file.getLinesAdded();
            linesDeleted += file.getLinesDeleted();
        }

        // Логування змін
        System.out.println("GitHub Commit: " + adapter.getSHA1());
        System.out.println("Added lines: " + linesAdded + ", Deleted lines: " + linesDeleted);

        metric.setLinesAdded(linesAdded);
        metric.setLinesDeleted(linesDeleted);
    }

    public void calculateLinesChangedForLocal(RevCommit commit, CommitMetric metric, Repository repository) throws IOException {
        int addedLines = 0, deletedLines = 0;

        // Отримуємо ID старого дерева, якщо є батьківський коміт
        ObjectId oldTreeId = commit.getParentCount() > 0 ? commit.getParent(0).getTree().getId() : null;
        ObjectId newTreeId = commit.getTree().getId();

        try (DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
            diffFormatter.setRepository(repository);

            // Ініціалізуємо парсери дерев, перевіряючи на null
            AbstractTreeIterator oldTreeParser = oldTreeId != null ? getTreeParser(repository, oldTreeId) : null;
            AbstractTreeIterator newTreeParser = getTreeParser(repository, newTreeId);

            // Перевірка на наявність обох дерев перед виконанням порівняння
            if (oldTreeParser != null && newTreeParser != null) {
                List<DiffEntry> entries = diffFormatter.scan(oldTreeParser, newTreeParser);
                for (DiffEntry entry : entries) {
                    FileHeader fileHeader = diffFormatter.toFileHeader(entry);
                    EditList editList = fileHeader.toEditList();
                    for (Edit edit : editList) {
                        addedLines += edit.getEndB() - edit.getBeginB();
                        deletedLines += edit.getEndA() - edit.getBeginA();
                    }
                }
            }
        }

        // Оновлюємо метрики коміту
        metric.setLinesAdded(addedLines);
        metric.setLinesDeleted(deletedLines);
    }


    // Оновлення метрик проекту
    private void updateProjectMetrics(ProjectMetrics projectMetrics) {
        List<CommitMetric> metrics = commitMetricRepository.findByProjectName(projectMetrics.getProjectName());

        if (metrics.isEmpty()) {
            System.out.println("Не знайдено метрик для проекту: " + projectMetrics.getProjectName());
            return;
        }

        double averageAdded = metrics.stream().mapToDouble(CommitMetric::getLinesAdded).average().orElse(0.0);
        double averageDeleted = metrics.stream().mapToDouble(CommitMetric::getLinesDeleted).average().orElse(0.0);

        System.out.println("Оновлення проекту: " + projectMetrics.getProjectName());
        System.out.println("Середнє доданих рядків: " + averageAdded);
        System.out.println("Середнє видалених рядків: " + averageDeleted);

        projectMetrics.setAverageLinesAdded(averageAdded);
        projectMetrics.setAverageLinesDeleted(averageDeleted);
    }

    // Метод для зчитування авторів з локального репозиторію
    public static Set<String> getAuthorsFromLocalRepository(String repositoryPath) throws IOException {
        Set<String> authors = new HashSet<>();

        try (Repository repository = new FileRepositoryBuilder()
                .setGitDir(new File(repositoryPath + "/.git"))
                .build();
             Git git = new Git(repository)) {

            Iterable<RevCommit> commits = git.log().all().call();
            for (RevCommit commit : commits) {
                authors.add(commit.getAuthorIdent().getName());
            }
        } catch (NoHeadException e) {
            throw new RuntimeException(e);
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }

        return authors;
    }

    // Метод для зчитування авторів з репозиторію на GitHub
    public static Set<String> getAuthorsFromGitHubRepository(String repositoryUrl, String gitHubToken) throws IOException {
        Set<String> authors = new HashSet<>();

        // Отримуємо GitHub репозиторій через GitHub API
        GitHub github = new GitHubBuilder().withOAuthToken(gitHubToken).build();
        String[] parts = repositoryUrl.split("/");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Невірний формат URL репозиторію: " + repositoryUrl);
        }

        // Створення об'єкта репозиторію на GitHub
        GHRepository repository = github.getRepository(parts[0] + "/" + parts[1]);

        // Отримуємо список комітів (можна обмежити кількість комітів або пройти по кількох сторінках)
        PagedIterator<GHCommit> commits = repository.listCommits().iterator();

        // Перебираємо всі коміти та додаємо авторів
        while (commits.hasNext()) {
            GHCommit commit = commits.next();
            String authorName = commit.getCommitter().getName();
            authors.add(authorName);
        }
        return authors;
    }

    private String getRepositoryPath(CommitAdapter adapter) throws IOException {
        if (adapter instanceof GHCommitAdapter) {
            GHCommitAdapter ghAdapter = (GHCommitAdapter) adapter;
            return ghAdapter.getRepositoryUrl(); // Використовуємо метод для отримання URL репозиторію з GHCommitAdapter
        } else if (adapter instanceof LocalCommitAdapter) {
            LocalCommitAdapter localAdapter = (LocalCommitAdapter) adapter;
            return localAdapter.getRepository().getDirectory().getPath(); // Отримуємо шлях до локального репозиторію
        }
        return "/default/path"; // Якщо не визначено тип репозиторію, повертаємо дефолтний шлях
    }
}
