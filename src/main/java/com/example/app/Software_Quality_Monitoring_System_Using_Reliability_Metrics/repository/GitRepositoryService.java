package com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.repository;

import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity.ProjectMetrics;
import org.kohsuke.github.GHCommit;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;



public interface GitRepositoryService {

    CompletableFuture<Void> saveCommitMetrics(String projectName, String repositoryPath, boolean isGitHubRepo) throws IOException;
    boolean isRepositoryAlreadySaved(String projectName);
    List<GHCommit> getCommitsForGitHubRepo(String projectName, String repositoryPath) throws IOException;
    ProjectMetrics getOrCreateProjectMetrics(String projectName);
    void updateProjectMetrics(String projectName) throws IOException;
    List<String> getFilesFromRepo(String projectName) throws IOException;
}
