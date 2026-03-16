/**
 * reports.js — логика страницы формирования отчётов.
 *
 * Что делает этот файл:
 * ─────────────────────
 * 1) При загрузке страницы:
 *    - Загружает список групп диагнозов из API (для выпадающего списка).
 *    - Устанавливает даты по умолчанию (1 января — сегодня текущего года).
 *
 * 2) При выборе типа отчёта:
 *    - Показывает/скрывает поле "Группа диагнозов"
 *      (некоторые отчёты требуют выбора группы, некоторые — нет).
 *    - Показывает описание отчёта (что он содержит).
 *    - Активирует кнопку "Сформировать".
 *
 * 3) При нажатии "Сформировать отчёт":
 *    - Валидирует поля (даты заполнены, группа выбрана если нужна).
 *    - Отправляет POST-запрос на /api/reports/generate.
 *    - Получает XLSX-файл как blob и запускает скачивание.
 */

// ═══════════════════════════════════════════════════
// ИНИЦИАЛИЗАЦИЯ (при загрузке страницы)
// ═══════════════════════════════════════════════════

document.addEventListener('DOMContentLoaded', async () => {
    // Загружаем группы диагнозов в выпадающий список
    await loadDiagnosisGroups();

    // Загружаем районы в выпадающий список (для фильтра "Район")
    await loadStates();

    // Устанавливаем даты по умолчанию: 1 января текущего года — сегодня
    setDefaultDates();

    // Навешиваем обработчик на все радиокнопки фильтра по району
    document.querySelectorAll('input[name="regionFilter"]').forEach(radio => {
        radio.addEventListener('change', onRegionFilterChange);
    });
});

/**
 * Загружает группы диагнозов из API и заполняет <select id="diagnosisGroupId">.
 *
 * API возвращает массив объектов: [{id: 1, name: "Сифилис", code: "SYPHILIS"}, ...]
 * Мы используем id как value, name как текст option.
 */
async function loadDiagnosisGroups() {
    try {
        // get() — функция из common.js, выполняет GET-запрос к API
        const groups = await get('/dictionaries/diagnosis-groups');

        const select = document.getElementById('diagnosisGroupId');

        // Первый option — "Выберите..." (уже есть в HTML)
        groups.forEach(group => {
            const option = document.createElement('option');
            option.value = group.id;
            option.textContent = group.name;
            select.appendChild(option);
        });
    } catch (error) {
        console.error('Ошибка загрузки групп диагнозов:', error);
        showToast('Не удалось загрузить группы диагнозов', 'error');
    }
}

/**
 * Устанавливает даты по умолчанию.
 *
 * dateFrom = 1 января текущего года
 * dateTo   = сегодня
 *
 * Это удобно: оператор открывает страницу — даты уже заполнены за текущий год.
 * Можно сразу выбрать отчёт и нажать "Сформировать".
 */
function setDefaultDates() {
    const today = new Date();
    const year = today.getFullYear();

    // Формат для <input type="date">: YYYY-MM-DD
    document.getElementById('dateFrom').value = `${year}-01-01`;
    document.getElementById('dateTo').value = today.toISOString().split('T')[0];
}

/**
 * Загружает список районов из API для фильтра "Район".
 *
 * API: GET /api/dictionaries/states → [{id: 1, name: "Абанский р-н"}, ...]
 */
async function loadStates() {
    try {
        const states = await get('/dictionaries/states');

        const select = document.getElementById('stateId');
        states.forEach(state => {
            const option = document.createElement('option');
            option.value = state.id;
            option.textContent = state.name;
            select.appendChild(option);
        });
    } catch (error) {
        console.error('Ошибка загрузки районов:', error);
    }
}

/**
 * Вызывается при переключении радиокнопок "Край / Красноярск / Район".
 *
 * Если выбран "Район" — показываем выпадающий список районов.
 * Иначе — скрываем его и сбрасываем выбор.
 */
function onRegionFilterChange() {
    const selected = document.querySelector('input[name="regionFilter"]:checked').value;
    const stateBlock = document.getElementById('stateBlock');

    if (selected === 'STATE') {
        stateBlock.style.display = '';
    } else {
        stateBlock.style.display = 'none';
        document.getElementById('stateId').value = '';
    }
}


// ═══════════════════════════════════════════════════
// РЕАКЦИЯ НА ВЫБОР ТИПА ОТЧЁТА
// ═══════════════════════════════════════════════════

/**
 * Описания отчётов — показываются как подсказка при выборе типа.
 * Ключ = value из <select id="reportType">.
 */
const REPORT_DESCRIPTIONS = {
    structure:
        'Структура заболеваемости по районам: всего, до 17 лет, до 14, до 1 года, 1-2, 3-6 (ДДУ), село. ' +
        'Требуется выбрать группу диагнозов (Сифилис, Гонорея и т.д.).',

    indicators:
        'Сводная таблица: районы × группы заболеваний. Показывает количество случаев по каждой болезни. ' +
        'Группа диагнозов НЕ нужна — отчёт охватывает все группы.',

    per100k:
        'Заболеваемость ИППП на 100 тысяч населения по районам. Использует данные о численности населения. ' +
        'Группа диагнозов НЕ нужна.',

    ippp_details:
        'Разбивка ИППП по диагнозам, полу и возрастным группам (0-1, 2-14, 15-17, 18-29, 30-39, 40+). ' +
        'Группа диагнозов НЕ нужна.',

    doctor_patients:
        'Список пациентов с ФИО, датой рождения, диагнозом, районом и врачом. ' +
        'Требуется выбрать группу диагнозов.',

    detailed:
        'Подробный отчёт с секциями: профиль врача, место выявления, соц. группа, тип осмотра, район. ' +
        'Для сифилиса столбцы — МКБ-коды, для остальных — муж/жен/всего/%. ' +
        'Требуется выбрать группу диагнозов.'
};

/**
 * Какие отчёты требуют выбора группы диагнозов.
 *
 * true  = нужно выбрать группу (structure, doctor_patients, detailed)
 * false = группа не нужна (indicators, per100k, ippp_details)
 */
const NEEDS_DIAGNOSIS_GROUP = {
    structure: true,
    indicators: false,
    per100k: false,
    ippp_details: false,
    doctor_patients: true,
    detailed: true
};

/**
 * Вызывается при каждом изменении <select id="reportType">.
 *
 * Что делает:
 * 1) Показывает/скрывает поле "Группа диагнозов".
 * 2) Показывает описание выбранного отчёта.
 * 3) Включает/выключает кнопку "Сформировать".
 */
function onReportTypeChange() {
    const reportType = document.getElementById('reportType').value;

    // ── Группа диагнозов: показать или скрыть ──
    const diagBlock = document.getElementById('diagnosisGroupBlock');
    const spacer = document.getElementById('spacer');

    if (reportType && NEEDS_DIAGNOSIS_GROUP[reportType]) {
        // Для этого отчёта нужна группа → показываем select
        diagBlock.style.display = '';
        spacer.style.display = '';
    } else {
        // Не нужна → скрываем select и сбрасываем выбор
        diagBlock.style.display = 'none';
        spacer.style.display = 'none';
        document.getElementById('diagnosisGroupId').value = '';
    }

    // ── Описание отчёта ──
    const descBlock = document.getElementById('reportDescription');
    const descText = document.getElementById('reportDescriptionText');

    if (reportType && REPORT_DESCRIPTIONS[reportType]) {
        descText.textContent = '💡 ' + REPORT_DESCRIPTIONS[reportType];
        descBlock.style.display = '';
    } else {
        descBlock.style.display = 'none';
    }

    // ── Кнопка "Сформировать": активна, если выбран тип ──
    document.getElementById('generateBtn').disabled = !reportType;
}


// ═══════════════════════════════════════════════════
// ГЕНЕРАЦИЯ ОТЧЁТА (скачивание XLSX)
// ═══════════════════════════════════════════════════

/**
 * Вызывается при нажатии кнопки "Сформировать отчёт".
 *
 * Алгоритм:
 * 1) Собирает параметры из формы.
 * 2) Валидирует (все обязательные поля заполнены).
 * 3) Отправляет POST на /api/reports/generate.
 * 4) Получает ответ как Blob (бинарные данные XLSX).
 * 5) Создаёт невидимую ссылку <a> и "кликает" по ней — браузер скачивает файл.
 */
async function generateReport() {
    const reportType = document.getElementById('reportType').value;
    const dateFrom = document.getElementById('dateFrom').value;
    const dateTo = document.getElementById('dateTo').value;
    const regionFilter = document.querySelector('input[name="regionFilter"]:checked').value;
    const diagnosisGroupId = document.getElementById('diagnosisGroupId').value;
    const stateId = document.getElementById('stateId').value;

    // ── Валидация ──

    if (!reportType) {
        showToast('Выберите тип отчёта', 'error');
        return;
    }

    if (!dateFrom || !dateTo) {
        showToast('Укажите период (дата начала и окончания)', 'error');
        return;
    }

    // Проверяем, что dateFrom <= dateTo
    if (dateFrom > dateTo) {
        showToast('Дата начала не может быть позже даты окончания', 'error');
        return;
    }

    // Для отчётов, требующих группу диагнозов — проверяем, что она выбрана
    if (NEEDS_DIAGNOSIS_GROUP[reportType] && !diagnosisGroupId) {
        showToast('Выберите группу диагнозов', 'error');
        return;
    }

    // Если выбран "Район" — нужно выбрать конкретный район
    if (regionFilter === 'STATE' && !stateId) {
        showToast('Выберите район из списка', 'error');
        return;
    }

    // ── Собираем тело запроса ──

    const requestBody = {
        reportType: reportType,
        dateFrom: dateFrom,
        dateTo: dateTo,
        regionFilter: regionFilter
    };

    // diagnosisGroupId добавляем только если он выбран (иначе null / пустая строка)
    if (diagnosisGroupId) {
        requestBody.diagnosisGroupId = parseInt(diagnosisGroupId);
    }

    // stateId добавляем только при фильтре "Район"
    if (regionFilter === 'STATE' && stateId) {
        requestBody.stateId = parseInt(stateId);
    }

    // ── Отправляем запрос ──

    const btn = document.getElementById('generateBtn');
    btn.disabled = true;
    btn.textContent = '⏳ Формирование...';

    try {
        // Здесь НЕ используем fetchApi из common.js,
        // потому что fetchApi ожидает JSON-ответ (response.json()),
        // а контроллер возвращает бинарный XLSX-файл (response.blob()).
        // Собираем заголовки. Нужен CSRF-токен, потому что это POST-запрос.
        // Здесь мы НЕ используем fetchApi из common.js, потому что fetchApi
        // ожидает JSON-ответ (response.json()), а контроллер возвращает
        // бинарный XLSX-файл (response.blob()).
        const headers = {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        };

        // Добавляем CSRF-токен (функция getCsrfToken из common.js)
        const csrfToken = getCsrfToken();
        if (csrfToken) {
            headers['X-XSRF-TOKEN'] = csrfToken;
        }

        const response = await fetch('/api/reports/generate', {
            method: 'POST',
            headers: headers,
            body: JSON.stringify(requestBody)
        });

        // Проверяем статус ответа
        if (!response.ok) {
            // Если ошибка — пытаемся прочитать текст ошибки
            const errorText = await response.text();
            throw new Error(errorText || `Ошибка ${response.status}`);
        }

        // ── Извлекаем имя файла из заголовка Content-Disposition ──
        //
        // Заголовок выглядит так:
        //   Content-Disposition: attachment; filename*=UTF-8''%D0%A1%D1%82%D1%80%D1%83%D0%BA...
        //
        // Нам нужно декодировать URL-encoded имя файла.
        const disposition = response.headers.get('Content-Disposition');
        let fileName = 'report.xlsx'; // Имя по умолчанию

        if (disposition) {
            // Ищем filename*=UTF-8'' (RFC 5987 формат)
            const utf8Match = disposition.match(/filename\*=UTF-8''(.+)/i);
            if (utf8Match) {
                fileName = decodeURIComponent(utf8Match[1]);
            } else {
                // Обычный filename="..."
                const plainMatch = disposition.match(/filename="?([^"]+)"?/i);
                if (plainMatch) {
                    fileName = plainMatch[1];
                }
            }
        }

        // ── Скачиваем файл ──
        //
        // response.blob() — получаем бинарные данные файла.
        // URL.createObjectURL(blob) — создаём временный URL для blob.
        // Создаём невидимый <a> с этим URL и атрибутом download, "кликаем" по нему.
        // Браузер начинает скачивание.

        const blob = await response.blob();
        const url = URL.createObjectURL(blob);

        const a = document.createElement('a');
        a.href = url;
        a.download = fileName;
        document.body.appendChild(a);
        a.click();

        // Убираем за собой: удаляем <a> и освобождаем URL
        document.body.removeChild(a);
        URL.revokeObjectURL(url);

        showToast('Отчёт сформирован и скачан', 'success');

    } catch (error) {
        console.error('Ошибка формирования отчёта:', error);
        showToast('Ошибка: ' + error.message, 'error');
    } finally {
        // Возвращаем кнопку в исходное состояние
        btn.disabled = false;
        btn.textContent = '📊 Сформировать отчёт';
    }
}
