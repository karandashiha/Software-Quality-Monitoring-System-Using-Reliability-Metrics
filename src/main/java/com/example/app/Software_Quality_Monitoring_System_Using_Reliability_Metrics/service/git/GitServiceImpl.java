package com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.service.git;

import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.repository.GitRepositoryService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/** Клас GitServiceImpl є сервісом, який відповідає за вибір між двома типами
 репозиторіїв і делегує завдання збереження метрик відповідному сервісу
— GitHubGitService або LocalGitService.*/


@Service
@Transactional
public class GitServiceImpl {

    private final GitHubGitService gitHubGitService;
    private final LocalGitService localGitService;

    @Autowired
    public GitServiceImpl(GitHubGitService gitHubGitService, LocalGitService localGitService) {
        this.gitHubGitService = gitHubGitService;
        this.localGitService = localGitService;
    }

    public CompletableFuture<Void> saveCommitMetrics(String projectName, String repositoryPath, boolean isGitHubRepo) throws IOException {
        GitRepositoryService service = selectService(isGitHubRepo);
        return service.saveCommitMetrics(projectName, repositoryPath, isGitHubRepo);
    }

    private GitRepositoryService selectService(boolean isGitHubRepo) {
        return isGitHubRepo ? gitHubGitService : localGitService;
    }
}