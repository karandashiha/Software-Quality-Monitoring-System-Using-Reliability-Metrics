Створення БД (git_metrics_db)
_____________________________________________________________________
1. Створення таблиці employees
Це дозволить зберігати інформацію про авторів комітів.

-- Таблиця employees
CREATE TABLE employees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    hourly_rate DOUBLE NOT NULL,
    role VARCHAR(255) NOT NULL,
    date_joined DATE,
    CONSTRAINT uc_employees_name UNIQUE (name)
);
_____________________________________________________________________
2. Створення таблиці project_metrics
 Таблиця project_metrics служить для зберігання агрегованих метрик по всьому
проекту. Вона містить статистику, розраховану на основі даних з таблиці commit_metrics,
і може використовуватися для надання загального огляду якості проекту.

-- Таблиця project_metrics
CREATE TABLE project_metrics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_name VARCHAR(255),
    average_lines_added DOUBLE,
    average_lines_deleted DOUBLE,
    last_updated DATETIME
);
_____________________________________________________________________
3. Створення таблиці commit_metrics

створюємо зв’язок між author_id з таблицею employees
-- Таблиця commit_metrics
CREATE TABLE commit_metrics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    commit_hash VARCHAR(255) NOT NULL,
    author_id BIGINT,
    commit_date DATETIME NOT NULL,
    lines_added INT NOT NULL,
    lines_deleted INT NOT NULL,
    project_name VARCHAR(255),
    file_type VARCHAR(255),
    project_metrics_id BIGINT,
    CONSTRAINT fk_commit_author FOREIGN KEY (author_id) REFERENCES employees (id),
    CONSTRAINT fk_commit_project_metrics FOREIGN KEY (project_metrics_id) REFERENCES project_metrics (id),
    INDEX idx_project_name (project_name)
);
_____________________________________________________________________
4. Зміна таблиці commit_metrics
Видаляємо текстове поле author, додаємо author_id та створюємо зв’язок з таблицею employees.

ALTER TABLE git_metrics_db.commit_metrics
DROP COLUMN author;

ALTER TABLE git_metrics_db.commit_metrics
ADD COLUMN author_id BIGINT,
ADD CONSTRAINT fk_author
FOREIGN KEY (author_id) REFERENCES git_metrics_db.employees (id);
_____________________________________________________________________
5. Додавання зв’язку між commit_metrics та project_metrics
Додаємо зовнішній ключ для project_name, посилаючись на таблицю project_metrics

ALTER TABLE git_metrics_db.commit_metrics
ADD CONSTRAINT fk_project_name
FOREIGN KEY (project_name) REFERENCES git_metrics_db.project_metrics (project_name);
_____________________________________________________________________
6. Створення таблиці teams
Ця таблиця дозволить пов'язувати тімліда з його командою та проектом.

-- Таблиця teams
CREATE TABLE teams (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_lead_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    project_name VARCHAR(255),
    repository_path VARCHAR(255) NOT NULL,
    project_metrics_id BIGINT NOT NULL,
    CONSTRAINT fk_team_lead FOREIGN KEY (team_lead_id) REFERENCES employees (id),
    CONSTRAINT fk_team_member FOREIGN KEY (member_id) REFERENCES employees (id),
    CONSTRAINT fk_team_project_metrics FOREIGN KEY (project_metrics_id) REFERENCES project_metrics (id)
);

