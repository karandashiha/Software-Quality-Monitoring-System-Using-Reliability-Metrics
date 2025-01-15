package com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.service.git.adapter;

import lombok.Getter;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHRepository;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/*Цей клас адаптує об'єкт GHCommit (який є частиною GitHub API) до інтерфейсу
CommitAdapter, забезпечуючи доступ до метрик комітів з GitHub.

Основні методи та їх функціональність:
getSHA1():

Повертає хеш коміту (SHA1) з об'єкта GHCommit.
getAuthorName():

Повертає ім'я автора коміту, отримане з метаданих коміту (GHCommit),
або генерує помилку вводу/виводу (IOException).
getCommitterDate():

Повертає дату коміта, коли коміт був здійснений.
getModifiedFiles():

Повертає список імен змінених файлів в коміт, адаптуючи їх через об'єкт GHCommit.File.
Поля класу:
ghCommit: Об'єкт GHCommit, що містить дані про коміт на GitHub.
fileType: Тип файлу, який було змінено у коміті.
Призначення:
Клас надає методи для доступу до властивостей коміту, таких як хеш коміту,
автор коміту, дата коміту, та список змінених файлів, що дозволяє
 інтегрувати дані з GitHub в загальну систему обробки метрик комітів.*/

public class GHCommitAdapter implements CommitAdapter {
    @Getter
    private final GHCommit ghCommit;
    @Getter
    private final String fileType;
    private final GHRepository repository;

    public GHCommitAdapter(GHCommit ghCommit, String fileType, GHRepository repository) {
        this.ghCommit = ghCommit;
        this.fileType = fileType;
        this.repository = repository;
    }

    @Override
    public String getSHA1() {
        return ghCommit.getSHA1();
    }

    @Override
    public String getAuthorName() throws IOException {
        return ghCommit.getCommitShortInfo().getAuthor().getName();
    }

    @Override
    public java.util.Date getCommitterDate() throws IOException {
        return ghCommit.getCommitDate();
    }

    public List<String> getModifiedFiles() throws IOException {
        return ghCommit.getFiles().stream()
                .map(GHCommit.File::getFileName)
                .collect(Collectors.toList());
    }


    //Повертає URL репозиторію, в якому знаходиться цей коміт.
    public String getRepositoryUrl() {
        return repository.getHtmlUrl().toString();
    }
}
