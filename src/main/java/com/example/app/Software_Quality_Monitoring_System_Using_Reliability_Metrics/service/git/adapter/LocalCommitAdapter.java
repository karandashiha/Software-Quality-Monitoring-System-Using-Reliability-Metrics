package com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.service.git.adapter;

import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.service.git.LocalGitService;
import lombok.Getter;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**Клас LocalCommitAdapter реалізує інтерфейс CommitAdapter та призначений для адаптації інформації про коміт з локального репозиторію Git до потрібного формату для інших частин програми. Ось короткий опис його функціоналу:

    Основні компоненти:
revCommit: Об'єкт типу RevCommit, що представляє коміт у локальному Git-репозиторії.
repository: Репозиторій Git, в якому знаходиться коміт.
fileType: Тип файлів, що змінені в комітті (наприклад, Java, XML тощо).
localGitService: Сервіс, який надає додаткові функції для роботи з Git, зокрема для отримання різниць між комітами.
    Методи:
getSHA1(): Повертає SHA1 (унікальний ідентифікатор) поточного коміту.
getAuthorName(): Повертає ім'я автора коміту.
getCommitterDate(): Повертає дату коміту.
getModifiedFiles(): Повертає список змінених файлів у комітті, порівнюючи з його батьківським комітом.
getCommit(): Повертає сам об'єкт коміту.
    Загальний функціонал:
Клас забезпечує доступ до інформації про коміт у вигляді, який потрібен для інших компонентів системи.
Використовує localGitService для отримання змінених файлів між комітами.
Підтримує основні властивості коміту, такі як SHA1, автор, дата та змінені файли.*/

public class LocalCommitAdapter implements CommitAdapter {

    private final RevCommit revCommit;
    // Отримання репозиторію
    @Getter
    private final Repository repository;
    // Отримання типу файлу
    @Getter
    private final String fileType;
    private final LocalGitService localGitService;

    public LocalCommitAdapter(RevCommit revCommit, Repository repository, String fileType, LocalGitService localGitService) {
        this.revCommit = revCommit;
        this.repository = repository;
        this.fileType = fileType;
        this.localGitService=localGitService;

    }

    @Override
    public String getSHA1() {
        return revCommit.getName();
    }

    @Override
    public String getAuthorName() {
        return revCommit.getCommitterIdent().getName();
    }

    @Override
    public java.util.Date getCommitterDate() {
        return revCommit.getCommitterIdent().getWhen();
    }

    @Override
    public List<String> getModifiedFiles() {
        if (revCommit.getParentCount() > 0) {
            RevCommit parentCommit = revCommit.getParent(0); // Отримання батьківського коміту
            List<DiffEntry> diffs = localGitService.getDiffs(parentCommit, revCommit, repository); // Виклик методу з сервісу
            List<String> modifiedFiles = new ArrayList<>();
            for (DiffEntry diff : diffs) {
                modifiedFiles.add(diff.getNewPath());
            }
            return modifiedFiles.isEmpty() ? Collections.emptyList() : modifiedFiles;
        }
        return Collections.emptyList();
    }

    // Отримання коміту
    public RevCommit getCommit() {
        return revCommit;
    }
}

