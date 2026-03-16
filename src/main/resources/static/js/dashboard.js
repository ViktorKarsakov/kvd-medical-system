/**
 * KVD Application - Dashboard (Главная страница)
 *
 * Загружает статистику с сервера и отображает:
 * 1) Карточки с числами (пациенты, случаи за месяц/год)
 * 2) Распределение по группам диагнозов (горизонтальные полоски)
 * 3) Таблица последних 10 добавленных случаев
 */

document.addEventListener('DOMContentLoaded', () => {
    loadDashboard();
});

async function loadDashboard() {
    try {
        const stats = await get('/dashboard/stats');
        renderStats(stats);
        renderDistribution(stats.diagnosisDistribution || []);
        renderMonthlyChart(stats.monthlyCases || []);
    } catch (error) {
        console.error('Ошибка загрузки дашборда:', error);
        showToast('Ошибка загрузки статистики', 'error');
    }
}

/**
 * Заполняет карточки со счётчиками.
 */
function renderStats(stats) {
    document.getElementById('statPatients').textContent = formatNumber(stats.totalPatients);
    document.getElementById('statCases').textContent = formatNumber(stats.totalCases);
    document.getElementById('statMonth').textContent = formatNumber(stats.casesThisMonth);
    document.getElementById('statYear').textContent = formatNumber(stats.casesThisYear);
}

/**
 * Рисует распределение по группам диагнозов.
 * Горизонтальные полоски с процентами — наглядно и без сторонних библиотек.
 */
function renderDistribution(data) {
    const container = document.getElementById('diagnosisDistribution');

    if (data.length === 0) {
        container.innerHTML = '<p style="color: #9ca3af; padding: 12px;">Нет данных за текущий год</p>';
        return;
    }

    // Находим максимум для масштабирования полосок
    const maxCount = Math.max(...data.map(d => d.count));

    container.innerHTML = '<div class="dist-chart">' +
        data.map(item => {
            const pct = maxCount > 0 ? Math.round((item.count / maxCount) * 100) : 0;
            return `
                <div class="dist-row">
                    <div class="dist-label">${escapeHtml(item.name)}</div>
                    <div class="dist-bar-wrap">
                        <div class="dist-bar" style="width: ${pct}%"></div>
                    </div>
                    <div class="dist-count">${item.count}</div>
                </div>
            `;
        }).join('') +
    '</div>';
}

/**
 * Форматирование числа с разделителями тысяч: 12345 → "12 345"
 */
function formatNumber(n) {
    if (n == null) return '—';
    return n.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ' ');
}

/**
 * Линейный график динамики заболеваемости по месяцам.
 * Использует Chart.js (подключается из CDN в index.html).
 *
 * @param {number[]} monthlyData — массив из 12 чисел (январь–декабрь)
 */
function renderMonthlyChart(monthlyData) {
    const ctx = document.getElementById('monthlyChart');
    if (!ctx) return;

    // Названия месяцев на русском
    const months = ['Янв', 'Фев', 'Мар', 'Апр', 'Май', 'Июн',
                     'Июл', 'Авг', 'Сен', 'Окт', 'Ноя', 'Дек'];

    // Если данных нет или все нули — показываем пустой график
    const data = monthlyData.length === 12 ? monthlyData : new Array(12).fill(0);

    new Chart(ctx, {
        type: 'line',
        data: {
            labels: months,
            datasets: [{
                label: 'Случаев',
                data: data,
                borderColor: '#2563eb',
                backgroundColor: 'rgba(37, 99, 235, 0.1)',
                borderWidth: 2,
                pointBackgroundColor: '#2563eb',
                pointRadius: 4,
                pointHoverRadius: 6,
                fill: true,
                tension: 0.3  // плавная кривая
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false  // одна линия — легенда не нужна
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return 'Случаев: ' + context.parsed.y;
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        stepSize: 1,
                        color: '#64748b'
                    },
                    grid: {
                        color: '#f1f5f9'
                    }
                },
                x: {
                    ticks: {
                        color: '#64748b'
                    },
                    grid: {
                        display: false
                    }
                }
            }
        }
    });
}
