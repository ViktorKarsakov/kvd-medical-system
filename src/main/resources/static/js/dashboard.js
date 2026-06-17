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
 await loadIpppDashboard();
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
 tension: 0.3 // плавная кривая
 }]
 },
 options: {
 responsive: true,
 maintainAspectRatio: false,
 plugins: {
 legend: {
 display: false // одна линия — легенда не нужна
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


// ──────────────────────────────────────────────────
// ИППП-ДАШБОРД
// ──────────────────────────────────────────────────

let ipppData = [];
let ipppChartInstance = null;

// Загружает список доступных годов и данные за первый (текущий)
async function loadIpppDashboard() {
 try {
 const years = await get('/dashboard/available-years');
 const select = document.getElementById('ipppYearSelect');
 if (!select || !years || years.length === 0) return;

 select.innerHTML = years.map(y => `<option value="${y}">${y}</option>`).join('');
 await loadIpppForYear(years[0]);
 } catch (e) {
 console.error('Ошибка ИППП:', e);
 }
}

// Вызывается при смене года в <select>
async function onIpppYearChange() {
 const year = parseInt(document.getElementById('ipppYearSelect').value, 10);
 if (!year) return;
 // Скрываем динамику — она была за другой год
 document.getElementById('ipppDynamicsSection').style.display = 'none';
 document.querySelectorAll('.ippp-row').forEach(r => r.classList.remove('ippp-row-active'));
 await loadIpppForYear(year);
}

// Загружает и рисует распределение за выбранный год
async function loadIpppForYear(year) {
 const container = document.getElementById('ipppDistribution');
 container.innerHTML = '<div class="loading"><div class="spinner"></div></div>';

 const data = await get('/dashboard/ippp-distribution?year=' + year);
 ipppData = data;

 if (!data || data.length === 0) {
 container.innerHTML = '<p style="color:#9ca3af;padding:12px;">Нет данных за выбранный год</p>';
 return;
 }

 const maxCount = Math.max(...data.map(d => d.count));
 container.innerHTML = '<div class="dist-chart">' +
 data.map((item, idx) => {
 const pct = maxCount > 0 ? Math.max(Math.round(item.count / maxCount * 100), 1) : 0;
 return `<div class="dist-row ippp-row" style="cursor:pointer;border-radius:6px;padding:6px 4px;transition:background .15s;"
 onmouseover="this.style.background='#f0f9ff'" onmouseout="if(!this.classList.contains('ippp-row-active'))this.style.background=''"
 onclick="loadIpppMonthly(${idx}, this)">
 <div class="dist-label">${escapeHtml(item.name)}</div>
 <div class="dist-bar-wrap"><div class="dist-bar" style="width:${pct}%;background:linear-gradient(90deg,#0ea5e9,#0369a1);"></div></div>
 <div class="dist-count">${item.count}</div>
 </div>`;
 }).join('') + '</div>';
}

// Загружает динамику по месяцам при клике на строку
async function loadIpppMonthly(idx, el) {
 const item = ipppData[idx];
 if (!item) return;

 document.querySelectorAll('.ippp-row').forEach(r => {
 r.classList.remove('ippp-row-active');
 r.style.background = '';
 });
 el.classList.add('ippp-row-active');
 el.style.background = '#e0f2fe';

 const year = parseInt(document.getElementById('ipppYearSelect').value, 10);
 const section = document.getElementById('ipppDynamicsSection');
 section.style.display = '';
 document.getElementById('ipppDynamicsTitle').textContent = 'Динамика: ' + item.name + ' (' + year + ')';
 section.scrollIntoView({ behavior: 'smooth', block: 'start' });

 const monthly = await get('/dashboard/ippp-monthly?groupCode=' + encodeURIComponent(item.code) + '&year=' + year);

 if (ipppChartInstance) { ipppChartInstance.destroy(); ipppChartInstance = null; }

 const ctx = document.getElementById('ipppMonthlyChart');
 const months = ['Янв','Фев','Мар','Апр','Май','Июн','Июл','Авг','Сен','Окт','Ноя','Дек'];
 const data = monthly.length === 12 ? monthly : new Array(12).fill(0);

 ipppChartInstance = new Chart(ctx, {
 type: 'line',
 data: { labels: months, datasets: [{ label: 'Случаев', data, borderColor: '#0ea5e9',
 backgroundColor: 'rgba(14,165,233,0.1)', borderWidth: 2,
 pointBackgroundColor: '#0ea5e9', pointRadius: 4, pointHoverRadius: 6,
 fill: true, tension: 0.3 }] },
 options: { responsive: true, maintainAspectRatio: false,
 plugins: { legend: { display: false }, tooltip: { callbacks: { label: c => 'Случаев: ' + c.parsed.y } } },
 scales: { y: { beginAtZero: true, ticks: { color: '#64748b' }, grid: { color: '#f1f5f9' } },
 x: { ticks: { color: '#64748b' }, grid: { display: false } } } }
 });
}
