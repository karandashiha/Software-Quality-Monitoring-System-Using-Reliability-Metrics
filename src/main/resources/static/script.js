
function fetchData(url, method = 'GET', bodyData = null, headers = {'Content-Type': 'application/json'}) {
    const options = {
        method,
        headers,
    };

    // Додаємо тіло запиту, якщо воно присутнє
    if (bodyData) {
        options.body = JSON.stringify(bodyData);
    }

    return fetch(url, options)
        .then(response => {
            if (!response.ok) {
                // Формування детальнішого повідомлення про помилку
                return response.text().then(errorText => {
                    throw new Error(`Помилка: ${response.status} ${response.statusText}. ${errorText}`);
                });
            }

            // Автоматичне визначення типу відповіді
            const contentType = response.headers.get('Content-Type');
            if (contentType && contentType.includes('application/json')) {
                return response.json(); // Якщо відповідь у форматі JSON
            } else {
                return response.text(); // Якщо відповідь у текстовому форматі
            }
        })
        .catch(error => {
            console.error('Сталася помилка при виконанні запиту:', error);
            throw error; // Передаємо помилку далі для обробки
        });
}

// Функція для обробки результатів та заповнення таблиці
function populateTable(tableBodyId, data, columns) {
    const tableBody = document.getElementById(tableBodyId);
    tableBody.innerHTML = ''; // Очищаємо попередні дані

    if (!data || data.length === 0) {
        const row = document.createElement('tr');
        const cell = document.createElement('td');
        cell.colSpan = columns.length;
        cell.textContent = 'Дані не знайдено';
        row.appendChild(cell);
        tableBody.appendChild(row);
        return;
    }

       // Обробляємо кожен елемент даних
       data.forEach(item => {
          const row = document.createElement('tr');
          columns.forEach(column => {
              const cell = document.createElement('td');
            // Обробка вкладених об'єктів
             if (column === 'teamLead') {
                 cell.textContent = item.teamLead
                     ? `${item.teamLead.name} (${item.teamLead.role})`
                     : 'Немає даних';
             } else if (column === 'member') {
                 cell.textContent = item.member
                     ? `${item.member.name} (${item.member.role})`
                     : 'Немає даних';
             } else {
                 cell.textContent = item[column] || ''; // Загальні поля
             }

             row.appendChild(cell);
         });

         tableBody.appendChild(row);
      });
}

// Функція для очищення попередніх результатів
function clearResponse() {

    // Очищення тексту для аналізу
    document.getElementById('analysis-response').innerText = '';

      // Очищаємо вміст усіх таблиць (tbody)
        document.querySelectorAll('.table tbody').forEach(tbody => tbody.innerHTML = '');
    // Приховуємо всі таблиці
    const tables = [
        'author-activity-table', 'metrics-table', 'commit-count-by-day-table',
        'file-type-frequency-table', 'average-lines-added-table',
        'average-lines-deleted-table', 'average-time-between-commits-table',
        'teams-table','leader-teams-table', 'employees-table',
        'employeesName-table'
    ];
    tables.forEach(tableId => {
        document.getElementById(tableId).classList.add('hidden');
    });
      tables.forEach(tableId => {
            const table = document.getElementById(tableId);
            if (table) {
                table.classList.add('hidden');
            }
        });

        // Приховуємо всі елементи з класом .alert
        document.querySelectorAll('.alert').forEach(alert => alert.classList.add('d-none'));
}

// Функція для приховування всіх графіків
function clearCharts() {
    // Сховати всі графіки
    document.querySelectorAll('.chart-container').forEach(chart => chart.classList.add('hidden'));
}

// Універсальна функція для відображення результату
function showResult(elementId, message, type = "info") {
    const resultElement = document.getElementById(elementId);
    resultElement.className = `alert alert-${type} mt-3`;
    resultElement.textContent = message;
    resultElement.classList.remove("d-none");
}
// ----------------Збереження коммітів---------------------
document.getElementById('save-commits-form').addEventListener('submit', function(event) {
    event.preventDefault();
    const projectName = document.getElementById('projectName').value;
    const repositoryPath = document.getElementById('repositoryPath').value;
    const isGitHubRepo = document.getElementById('gitHubRepo').checked;

    // Формуємо URL для запиту залежно від вибору типу репозиторію
    const url = `http://localhost:8080/api/git/save-commits?projectName=${encodeURIComponent(projectName)}&repositoryPath=${encodeURIComponent(repositoryPath)}&isGitHubRepo=${isGitHubRepo}`;

    // Показуємо спінер
    const spinner = document.getElementById('save-spinner');
    spinner.classList.remove('d-none');

    // Ховаємо попередній статус відповіді
    const responseElement = document.getElementById('save-commits-response');
    responseElement.classList.add('d-none');

    // Виконуємо запит
    fetch(url, {
        method: 'POST'
    })
    .then(response => {
        const contentType = response.headers.get('Content-Type');
        if (!response.ok) {
            // Якщо статус відповіді не ок, викидаємо помилку
            throw new Error(`Помилка: ${response.status} ${response.statusText}`);
        }
        if (contentType && contentType.includes('application/json')) {
            return response.json(); // Якщо відповідь у форматі JSON
        } else {
            return response.text(); // Якщо це простий текст
        }
    })
    .then(data => {
               responseElement.classList.remove('d-none');
               spinner.classList.add('d-none'); // Ховаємо спінер

               if (typeof data === 'string') {
                   responseElement.classList.remove('alert-danger');
                   responseElement.classList.add('alert-success');
                   responseElement.innerText = data;
               } else {
                   responseElement.classList.remove('alert-danger');
                   responseElement.classList.add('alert-success');
                   responseElement.innerText = 'Коміти успішно збережено';
               }
           })
           .catch(error => {
               responseElement.classList.remove('d-none');
               spinner.classList.add('d-none'); // Ховаємо спінер

               responseElement.classList.remove('alert-success');
               responseElement.classList.add('alert-danger');

               if (error.message.includes('409')) {
                   responseElement.innerText = 'Репозиторій вже був збережений. Спробуйте інший.';
               } else {
                   responseElement.innerText = 'Сталася помилка: ' + error.message;
               }
           });
});


// ----------------Отримати всі метрики комітів---------------------
document.getElementById('get-metrics').addEventListener('click', function() {
    clearResponse(); // Очищаємо попередні результати
    const projectName = document.getElementById('analysisProjectName').value;

    if (!isProjectNameValid()) {
        alert('Введіть назву проекту');
        return;
    }

    fetchData(`http://localhost:8080/api/metrics/project?projectName=${encodeURIComponent(projectName)}`)
        .then(data => {
            console.log('Received data:', data);
            populateTable('metrics-body', data,  ['commitHash', 'author', 'projectName', 'commitDate', 'linesAdded', 'linesDeleted', 'fileType' ]);
            document.getElementById('metrics-table').classList.remove('hidden');
            // Оновлення графіка
            updateChart(data);
        })
        .catch(error => {
            console.error('Error occurred:', error);
            document.getElementById('analysis-response').innerText = "Сталася помилка: " + error.message;
            document.getElementById('analysis-response').classList.remove('d-none');
        });
});
// Функція для оновлення графіка
function updateChart(data) {

    clearCharts();  // Приховуємо всі графіки
    // Отримуємо контекст для графіка
    const ctx = document.getElementById('myChart').getContext('2d');

    // Збираємо дані для графіка
    const commitDates = data.map(item => item.commitDate);  // Дати комітів
    const linesAdded = data.map(item => item.linesAdded);   // Кількість доданих рядків

    // Створюємо графік
    const myChart = new Chart(ctx, {
        type: 'line',  // Лінійний графік
        data: {
            labels: commitDates,  // Дати комітів
            datasets: [{
                label: 'Кількість Доданих Рядків',
                data: linesAdded,  // Дані для графіка (додані рядки)
                borderColor: 'rgba(75, 192, 192, 1)',  // Колір лінії
                borderWidth: 1,
                fill: false  // Без заповнення під лінією
            }]
        },
        options: {
            responsive: true,
            scales: {
                x: {
                    title: {
                        display: true,
                        text: 'Дата Коміту'
                    }
                },
                y: {
                    title: {
                        display: true,
                        text: 'Кількість Рядків'
                    }
                }
            }
        }
    });
    // Показуємо графік
        document.querySelector('.chart-container#myChart-container').classList.remove('hidden');

}
// ----------------Середня кількість доданих рядків---------------------
document.getElementById('get-average-lines-added').addEventListener('click', function() {
    clearResponse(); // Очищаємо попередні результати
    const projectName = document.getElementById('analysisProjectName').value;

    if (!isProjectNameValid()) {
        alert('Введіть назву проекту');
        return;
    }

    fetchData(`http://localhost:8080/api/analysis/average-lines-added?projectName=${encodeURIComponent(projectName)}`)
        .then(data => {
            const tableBody = document.getElementById('average-lines-added-body');
            tableBody.innerHTML = `<tr><td>${data}</td></tr>`;
            document.getElementById('average-lines-added-table').classList.remove('hidden');
            // Оновлення графіка
            updateAverageLinesAddedChart(parseFloat(data));
        })
        .catch(error => {
            document.getElementById('analysis-response').innerText = "Сталася помилка: " + error.message;
            document.getElementById('analysis-response').classList.remove('d-none');
        });
});
function updateAverageLinesAddedChart(data) {
     clearCharts();  // Приховуємо всі графіки
     const ctx = document.getElementById('averageLinesAddedChart').getContext('2d');

    // Створюємо графік
    const chart = new Chart(ctx, {
        type: 'bar', // Стовпчиковий графік
        data: {
            labels: ['Середня кількість доданих рядків'], // Мітки
            datasets: [{
                label: 'Додані рядки',
                data: [data], // Данні для графіка
                backgroundColor: 'rgba(75, 192, 192, 0.2)',
                borderColor: 'rgba(75, 192, 192, 1)',
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });
    // Показуємо графік
    document.querySelector('.chart-container#averageLinesAddedChart-container').classList.remove('hidden');

}
// ----------------Середня кількість видалених рядків---------------------
document.getElementById('get-average-lines-deleted').addEventListener('click', function() {
    clearResponse(); // Очищаємо попередні результати
    const projectName = document.getElementById('analysisProjectName').value;

    if (!isProjectNameValid()) {
        alert('Введіть назву проекту');
        return;
    }

    fetchData(`http://localhost:8080/api/analysis/average-lines-deleted?projectName=${encodeURIComponent(projectName)}`)
        .then(data => {
            const tableBody = document.getElementById('average-lines-deleted-body');
            tableBody.innerHTML = `<tr><td>${data}</td></tr>`;
            document.getElementById('average-lines-deleted-table').classList.remove('hidden');
            // Оновлення графіка
            updateAverageLinesDeletedChart(parseFloat(data));
        })
        .catch(error => {
            document.getElementById('analysis-response').innerText = "Сталася помилка: " + error.message;
            document.getElementById('analysis-response').classList.remove('d-none');
        });
});
function updateAverageLinesDeletedChart(data) {
     clearCharts();  // Приховуємо всі графіки
     const ctx = document.getElementById('averageLinesDeletedChart').getContext('2d');

    // Створюємо графік
    const chart = new Chart(ctx, {
        type: 'bar', // Стовпчиковий графік
        data: {
            labels: ['Середня кількість видалених рядків'], // Мітки
            datasets: [{
                label: 'Видалені рядки',
                data: [data], // Данні для графіка
                backgroundColor: 'rgba(255, 99, 132, 0.2)',
                borderColor: 'rgba(255, 99, 132, 1)',
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });
    // Показуємо графік
    document.querySelector('.chart-container#averageLinesDeletedChart-container').classList.remove('hidden');

}
// ----------------Активність авторів---------------------
document.getElementById('get-author-activity').addEventListener('click', function() {
    clearResponse(); // Очищаємо попередні результати
    const projectName = document.getElementById('analysisProjectName').value;

    if (!isProjectNameValid()) {
        alert('Введіть назву проекту');
        return;
    }

    fetchData(`http://localhost:8080/api/analysis/author-activity?projectName=${encodeURIComponent(projectName)}`)
        .then(data => {
            populateTable('author-activity-body', Object.entries(data).map(([author, activity]) => ({ author, activity })), ['author', 'activity']);
            document.getElementById('author-activity-table').classList.remove('hidden');
            // Оновлення графіка
            updateAuthorActivityChart(data);
        })
        .catch(error => {
            document.getElementById('analysis-response').innerText = "Сталася помилка: " + error.message;
            document.getElementById('analysis-response').classList.remove('d-none');
        });
});
function updateAuthorActivityChart(data) {
     clearCharts();  // Приховуємо всі графіки
     const ctx = document.getElementById('authorActivityChart').getContext('2d');

    // Отримуємо авторів і їх активність
    const authors = Object.keys(data);
    const activity = Object.values(data);

    // Створюємо графік
    const chart = new Chart(ctx, {
        type: 'bar', // Стовпчиковий графік
        data: {
            labels: authors, // Автори
            datasets: [{
                label: 'Активність Авторів',
                data: activity, // Активність авторів
                backgroundColor: 'rgba(153, 102, 255, 0.2)',
                borderColor: 'rgba(153, 102, 255, 1)',
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });
   // Показуємо графік
   document.querySelector('.chart-container#authorActivityChart-container').classList.remove('hidden');

}
// ----------------Середній час між комітами---------------------
document.getElementById('get-average-time-between-commits').addEventListener('click', function() {
    clearResponse(); // Очищаємо попередні результати
    const projectName = document.getElementById('analysisProjectName').value;

    if (!isProjectNameValid()) {
        alert('Введіть назву проекту');
        return;
    }

    fetchData(`http://localhost:8080/api/analysis/average-time-between-commits?projectName=${encodeURIComponent(projectName)}`)
        .then(data => {
            const tableBody = document.getElementById('average-time-between-commits-body');
            tableBody.innerHTML = `<tr><td>${data}</td></tr>`;
            document.getElementById('average-time-between-commits-table').classList.remove('hidden');
            // Оновлення графіка
            updateAverageTimeBetweenCommitsChart(parseFloat(data));
        })
        .catch(error => {
            document.getElementById('analysis-response').innerText = "Сталася помилка: " + error.message;
            document.getElementById('analysis-response').classList.remove('d-none');
        });
});
function updateAverageTimeBetweenCommitsChart(data) {
    clearCharts(); // Приховуємо всі графіки

    const ctx = document.getElementById('averageTimeBetweenCommitsChart').getContext('2d');

    // Приклад даних для графіка (один бар із середнім значенням)
    const chartData = {
        labels: ['Середній Час'], // Назва графіка
        datasets: [{
            label: 'Середній Час Між Комітами',
            data: [data], // Вставляємо отримане значення
            backgroundColor: 'rgba(75, 192, 192, 0.2)',
            borderColor: 'rgba(75, 192, 192, 1)',
            borderWidth: 1
        }]
    };

    // Створюємо графік
    const chart = new Chart(ctx, {
        type: 'bar', // Тип графіка: стовпчиковий
        data: chartData,
        options: {
            responsive: true,
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });

    // Показуємо контейнер з графіком
    document.querySelector('.chart-container#averageTimeBetweenCommitsChart-container').classList.remove('hidden');
}
// ----------------Розподіл комітів по дням тижня---------------------
document.getElementById('get-commit-count-by-day').addEventListener('click', function() {
    clearResponse(); // Очищаємо попередні результати
    const projectName = document.getElementById('analysisProjectName').value;

    if (!isProjectNameValid()) {
        alert('Введіть назву проекту');
        return;
    }

    fetchData(`http://localhost:8080/api/analysis/commit-count-by-day?projectName=${encodeURIComponent(projectName)}`)
        .then(data => {
            populateTable('commit-count-by-day-body', Object.entries(data).map(([day, count]) => ({ day, count })), ['day', 'count']);
            document.getElementById('commit-count-by-day-table').classList.remove('hidden');
            // Оновлення графіка
            updateCommitCountByDayChart(data);
        })
        .catch(error => {
            document.getElementById('analysis-response').innerText = "Сталася помилка: " + error.message;
            document.getElementById('analysis-response').classList.remove('d-none');
        });
});
function updateCommitCountByDayChart(data) {
     clearCharts();  // Приховуємо всі графіки
     const ctx = document.getElementById('commitCountByDayChart').getContext('2d');

    // Отримуємо дні тижня і кількість комітів
    const days = Object.keys(data);
    const counts = Object.values(data);

    // Створюємо графік
    const chart = new Chart(ctx, {
        type: 'bar', // Стовпчиковий графік
        data: {
            labels: days, // Дні тижня
            datasets: [{
                label: 'Кількість Комітів',
                data: counts, // Кількість комітів
                backgroundColor: 'rgba(255, 159, 64, 0.2)',
                borderColor: 'rgba(255, 159, 64, 1)',
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });
    // Показуємо графік
    document.querySelector('.chart-container#commitCountByDayChart-container').classList.remove('hidden');

}
// ----------------Отримання частоти типів файлів---------------------
document.getElementById('get-file-type-frequency').addEventListener('click', function() {
    clearResponse(); // Очищаємо попередні результати
    const projectName = document.getElementById('analysisProjectName').value;

    if (!isProjectNameValid()) {
        alert('Введіть назву проекту');
        return;
    }

    fetchData(`http://localhost:8080/api/analysis/file-type-frequency?projectName=${encodeURIComponent(projectName)}`)
        .then(data => {
            populateTable('file-type-frequency-body', Object.entries(data).map(([fileType, count]) => ({ fileType, count })), ['fileType', 'count']);
            document.getElementById('file-type-frequency-table').classList.remove('hidden');
            // Оновлення графіка
            updateFileTypeFrequencyChart(data)
        })
        .catch(error => {
            document.getElementById('analysis-response').innerText = "Сталася помилка: " + error.message;
            document.getElementById('analysis-response').classList.remove('d-none');
        });
});
function updateFileTypeFrequencyChart(data) {
     clearCharts();  // Приховуємо всі графіки
     const ctx = document.getElementById('fileTypeFrequencyChart').getContext('2d');

    // Отримуємо типи файлів і їх кількість
    const fileTypes = Object.keys(data);
    const counts = Object.values(data);

    // Створюємо графік
    const chart = new Chart(ctx, {
        type: 'pie', // Кругова діаграма
        data: {
            labels: fileTypes, // Типи файлів
            datasets: [{
                label: 'Частота Типів Файлів',
                data: counts, // Кількість кожного типу
                backgroundColor: ['#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#FF9F40'],
                hoverBackgroundColor: ['#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#FF9F40']
            }]
        },
        options: {
            responsive: true
        }
    });
    // Показуємо графік
    document.querySelector('.chart-container#fileTypeFrequencyChart-container').classList.remove('hidden');

}
// ----------------Отримати команди за лідером---------------------
document.getElementById('get-teams-by-lead').addEventListener('click', function () {
    clearResponse();
    const teamLeadId = document.getElementById('leaderId').value.trim(); // Отримання значення з поля введення
    if (!teamLeadId) {
        alert("Будь ласка, введіть ID лідера команди.");
        return;
    }

    fetch(`http://localhost:8080/api/teams/team-lead/${encodeURIComponent(teamLeadId)}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Лідера не знайдено.');
            }
            return response.json();
        })
        .then(data => {
            console.log('Teams by lead:', data);
            populateTable('leader-teams-body', data, ['id', 'teamLead','member']);
            document.getElementById('leader-teams-table').classList.remove('hidden');
        })
        .catch(error => {
            console.error('Error fetching teams by lead:', error);
            alert('Помилка при отриманні даних: ' + error.message);
        });
});

// ----------------Отримати команди за проектом---------------------
document.getElementById('get-teams-by-project').addEventListener('click', function() {
    clearResponse();
    const projectName = document.getElementById('projectNameTeam').value.trim(); // Отримання значення з поля введення
    if (!projectName) {
        alert("Будь ласка, введіть назву проекту.");
        return;
    }

    fetch(`http://localhost:8080/api/teams/project/${encodeURIComponent(projectName)}`)
        .then(response => {
                if (!response.ok) {
                    throw new Error('Проект не знайдено.');
                }
                return response.json();
            })
        .then(data => {
            console.log('Teams by project:', data);
            populateTable('teams-body', data, ['id', 'projectName', 'teamLead', 'member']);
            document.getElementById('teams-table').classList.remove('hidden');
        })
        .catch(error => {
            console.error('Error fetching teams by project:', error);
            alert('Помилка при отриманні даних: ' + error.message);
        });
});

// -----------------Створення працівника----------------------------
document.getElementById('create-employee-btn').addEventListener('click', function() {
    clearResponse(); // Очищаємо попередні результати

    const newEmployeeName = document.getElementById('newEmployeeName').value.trim(); // Отримуємо ім'я працівника
    const newProjectName = document.getElementById('newProjectName').value.trim(); // Отримуємо назву проекту

    // Перевірка на порожні поля
    if (!newEmployeeName || !newProjectName) {
        showResult("create-employee-result", "Будь ласка, введіть ім'я працівника та назву проекту.", "warning");
        return;
    }

     // Формуємо запит до контролера, передаючи параметри через URL
        const url = `http://localhost:8080/api/teams/create-and-assign-to-project?name=${encodeURIComponent(newEmployeeName)}&projectName=${encodeURIComponent(newProjectName)}`;

        fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded', // Вказуємо тип вмісту
            },
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Не вдалося створити працівника та додати до проекту.');
            }
            return response.text();
        })
        .then(data => {
            console.log('Employee created and assigned:', data);
            showResult("create-employee-result", data, "success"); // Показуємо результат
        })
        .catch(error => {
            console.error('Error creating and assigning employee:', error);
            showResult("create-employee-result", error.message, "danger"); // Показуємо помилку
        });
    });

// ----------------Отримати всіх працівників за проект---------------
document.getElementById('get-employees-by-project').addEventListener('click', function() {
    clearResponse();
    const projectName = document.getElementById('employeeProjectName').value.trim(); // Отримуємо значення поля введення
    if (!projectName) {
        showResult("project-result", "Будь ласка, введіть назву проекту.", "warning");
        return;
    }
    fetch(`http://localhost:8080/api/employees/project/${encodeURIComponent(projectName)}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Працівників для вказаного проекту не знайдено.');
            }
            return response.json();
        })
        .then(data => {
            console.log('Employees by project:', data);
             if (data.length > 0) {
                // Сортуємо дані за ID перед відображенням
                data.sort((a, b) => a.id - b.id); // Для зростання ID, або b.id - a.id для спаду

                populateTable('employees-body', data, ['id', 'name', 'role', 'dateJoined', 'hourlyRate']);
                document.getElementById('employees-table').classList.remove('hidden');
            } else {
                showResult("project-result", `Працівників для проекту '${projectName}' не знайдено.`, "warning");
            }
        })
        .catch(error => {
            console.error('Error fetching employees by project:', error);
            showResult("project-result", error.message, "danger");
        });
});
// ----------------Отримати працівника за ім'ям---------------------
document.getElementById('get-employee-by-name').addEventListener('click', function() {
    clearResponse();
    const employeeName = document.getElementById('employeeName').value.trim(); // Отримання значення з поля введення
    if (!employeeName) {
        showResult("name-result", "Будь ласка, введіть ім'я працівника.", "warning");
        return;
    }

    fetch(`http://localhost:8080/api/employees/${encodeURIComponent(employeeName)}`)
         .then(response => {
                    if (!response.ok) {
                        throw new Error('Працівника не знайдено.');
                    }
                    return response.json();
                })
        .then(data => {
            console.log('Employee by name:', data);
            populateTable('employeesName-body', [data], ['id', 'name', 'role', 'dateJoined', 'hourlyRate']);
            document.getElementById('employeesName-table').classList.remove('hidden');
            showResult("name-result", `Працівник '${data.name}' знайдений.`, "success");

        })
        .catch(error => {
            console.error('Error fetching employee by name:', error);
            showResult("name-result", error.message, "danger");
        });
});

// ----------------Оновлення ставки працівника----------------------
document.getElementById('update-employee-rate').addEventListener('click', function() {
    clearResponse();
    const employeeIdForRateUpdate = document.getElementById('employeeIdForRateUpdate').value.trim(); // Отримуємо ID працівника

    if (!employeeIdForRateUpdate) {
        showResult("rate-update-result", "Будь ласка, введіть ID працівника для оновлення ставки.", "warning");
        return;
    }

    fetch(`http://localhost:8080/api/employees/${encodeURIComponent(employeeIdForRateUpdate)}/update-rate`, {
        method: 'PUT'
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Не вдалося оновити ставку працівника.');
        }
         return response.text();
    })
    .then(message => {
        showResult("rate-update-result", message, "success");
    })
    .catch(error => {
        console.error('Error updating employee rate:', error);
        showResult("rate-update-result", error.message, "danger");
    });
});

// ----------------Отримання ефективності працівника----------------
document.getElementById('get-employee-efficiency').addEventListener('click', function() {
    clearResponse();
    const employeeId = document.getElementById('employeeIdForEfficiency').value.trim(); // Отримуємо ID працівника

    if (!employeeId) {
        showResult("efficiency-result", "Будь ласка, введіть ID працівника.", "warning");
        return;
    }

    fetch(`http://localhost:8080/api/employees/${encodeURIComponent(employeeId)}/efficiency`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Не вдалося отримати ефективність працівника.');
            }
            return response.text();
        })
        .then(message => {
            showResult("efficiency-result", message, "success");
        })
        .catch(error => {
            console.error('Error fetching employee efficiency:', error);
            showResult("efficiency-result", error.message, "danger");
        });
});

// ----------------Отримання внеску працівника в проект---------------------
document.getElementById('get-employee-contribution').addEventListener('click', function() {
    clearResponse();
    const employeeId = document.getElementById('employeeIdForContribution').value.trim();
    const projectName = document.getElementById('projectNameForContribution').value.trim();

    if (!employeeId || !projectName) {
        showResult("contribution-result", "Будь ласка, введіть ID працівника та назву проекту.", "warning");
        return;
    }

    fetch(`http://localhost:8080/api/employees/${encodeURIComponent(employeeId)}/contribution?projectName=${encodeURIComponent(projectName)}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Не вдалося отримати внесок працівника.');
            }
            return response.text();
        })
        .then(message => {
            showResult("contribution-result", message, "success");
        })
        .catch(error => {
            console.error('Error fetching employee contribution:', error);
            showResult("contribution-result", error.message, "danger");
        });
});

// Функція для перевірки заповнення поля
function isProjectNameValid() {
    return document.getElementById('analysisProjectName').value.trim() !== '';
}

// Оновлення обробників подій для кнопок
const buttons = [
    { id: 'get-average-lines-added', message: 'середньої кількості доданих рядків' },
    { id: 'get-average-lines-deleted', message: 'середньої кількості видалених рядків' },
    { id: 'get-author-activity', message: 'активності авторів' },
    { id: 'get-average-time-between-commits', message: 'середнього часу між комітами' },
    { id: 'get-commit-count-by-day', message: 'розподілу комітів по дням тижня' },
    { id: 'get-file-type-frequency', message: 'частоти типів файлів' },
];

buttons.forEach(button => {
    document.getElementById(button.id).addEventListener('click', function() {
        if (!isProjectNameValid()) {
            alert('Введіть назву проекту');
            console.log('Назва проекту невірна')
            return;
        }
        clearResponse(); // Очищаємо попередні результати
    });
});
