/**
 * KVD Application - Patient Search
 */

let searchResults = [];
let currentPage = 0;
let totalPages = 0;
let pageSize = 20;

// Ключ для хранения состояния поиска в sessionStorage.
// sessionStorage живёт пока открыта вкладка браузера —
// при закрытии вкладки или браузера данные очищаются автоматически.
const SEARCH_STATE_KEY = 'kvd_search_state';

document.addEventListener('DOMContentLoaded', () => {
    initSearchPage();
});

async function initSearchPage() {
    // Загружаем справочники для фильтров
    await loadSearchFilters();

    // Проверяем: есть ли сохранённое состояние поиска в sessionStorage?
    // Это происходит когда пользователь вернулся с карточки пациента назад.
    const savedState = sessionStorage.getItem(SEARCH_STATE_KEY);

    if (savedState) {
        // Восстанавливаем сохранённое состояние
        restoreSearchState(JSON.parse(savedState));
    } else {
        // Первый вход на страницу — устанавливаем даты по умолчанию (текущий месяц)
        const today = new Date();
        const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);
        document.getElementById('createdFrom').value = firstDay.toISOString().split('T')[0];
        document.getElementById('createdTo').value = today.toISOString().split('T')[0];
    }
}

// Сохраняет текущее состояние фильтров и результатов поиска в sessionStorage.
// Вызывается перед переходом на карточку пациента.
function saveSearchState() {
    const state = {
        // Значения всех фильтров
        filters: {
            lastName: document.getElementById('filterLastName').value,
            firstName: document.getElementById('filterFirstName').value,
            middleName: document.getElementById('filterMiddleName').value,
            gender: document.getElementById('filterGender').value,
            state: document.getElementById('filterState').value,
            diagnosis: document.getElementById('filterDiagnosis').value,
            diagnosisGroup: document.getElementById('filterDiagnosisGroup').value,
            doctor: document.getElementById('filterDoctor').value,
            socialGroup: document.getElementById('filterSocialGroup').value,
            ageFrom: document.getElementById('filterAgeFrom').value,
            ageTo: document.getElementById('filterAgeTo').value,
            createdFrom: document.getElementById('createdFrom').value,
            createdTo: document.getElementById('createdTo').value,
        },
        // Текущая страница пагинации
        currentPage: currentPage,
        // Результаты поиска (чтобы не делать повторный запрос при возврате)
        searchResults: searchResults,
        // Общее количество страниц
        totalPages: totalPages,
    };

    // Сохраняем в sessionStorage как JSON-строку
    sessionStorage.setItem(SEARCH_STATE_KEY, JSON.stringify(state));
}

// Восстанавливает состояние поиска из сохранённых данных.
// Заполняет фильтры и отображает результаты без повторного запроса к серверу.
function restoreSearchState(state) {
    // Восстанавливаем значения фильтров
    const f = state.filters;
    document.getElementById('filterLastName').value = f.lastName || '';
    document.getElementById('filterFirstName').value = f.firstName || '';
    document.getElementById('filterMiddleName').value = f.middleName || '';
    document.getElementById('filterGender').value = f.gender || '';
    document.getElementById('filterState').value = f.state || '';
    document.getElementById('filterDiagnosis').value = f.diagnosis || '';
    document.getElementById('filterDiagnosisGroup').value = f.diagnosisGroup || '';
    document.getElementById('filterDoctor').value = f.doctor || '';
    document.getElementById('filterSocialGroup').value = f.socialGroup || '';
    document.getElementById('filterAgeFrom').value = f.ageFrom || '';
    document.getElementById('filterAgeTo').value = f.ageTo || '';
    document.getElementById('createdFrom').value = f.createdFrom || '';
    document.getElementById('createdTo').value = f.createdTo || '';

    // Восстанавливаем переменные состояния
    currentPage = state.currentPage || 0;
    searchResults = state.searchResults || [];
    totalPages = state.totalPages || 0;

    // Отображаем результаты без запроса к серверу
    renderSearchResults();

    // Восстанавливаем пагинацию
    renderPagination({
        totalElements: searchResults.length + (currentPage * pageSize),
        totalPages: totalPages,
    });
}

async function loadSearchFilters() {
    try {
        await Promise.all([
            loadSelectOptions('filterGender', '/dictionaries/genders', 'name', 'id', 'Любой'),
            loadSelectOptions('filterState', '/dictionaries/states', 'name', 'id', 'Любой'),
            loadSelectOptions('filterDiagnosis', '/dictionaries/diagnoses', 'name', 'id', 'Любой'),
            loadSelectOptions('filterDiagnosisGroup', '/dictionaries/diagnosis-groups', 'name', 'id', 'Любая'),
            loadSelectOptions('filterSocialGroup', '/dictionaries/social-groups', 'name', 'id', 'Любая'),
        ]);

        await loadDoctorsSelectForSearch();
    } catch (error) {
        console.error('Error loading filters:', error);
    }
}

async function loadDoctorsSelectForSearch() {
    const select = document.getElementById('filterDoctor');
    if (!select) return;

    try {
        const doctors = await get('/dictionaries/doctors');
        select.innerHTML = '<option value="">Любой</option>';

        doctors.forEach(doctor => {
            const option = document.createElement('option');
            option.value = doctor.id;

            let fullName = doctor.lastName || '';
            if (doctor.firstName) fullName += ` ${doctor.firstName.charAt(0)}.`;
            if (doctor.middleName) fullName += `${doctor.middleName.charAt(0)}.`;

            option.textContent = fullName;
            select.appendChild(option);
        });
    } catch (error) {
        console.error('Error loading doctors:', error);
    }
}

async function searchPatients(resetPage = true) {
    try {
        if (resetPage) {
            currentPage = 0;
        }

        showLoading('searchResults');

        const filters = {
            lastName: document.getElementById('filterLastName').value.trim() || null,
            firstName: document.getElementById('filterFirstName').value.trim() || null,
            middleName: document.getElementById('filterMiddleName').value.trim() || null,
            genderId: getSelectValue('filterGender'),
            stateId: getSelectValue('filterState'),
            diagnosisId: getSelectValue('filterDiagnosis'),
            diagnosisGroupId: getSelectValue('filterDiagnosisGroup'),
            doctorId: getSelectValue('filterDoctor'),
            socialGroupId: getSelectValue('filterSocialGroup'),
            ageFrom: getNumberValue('filterAgeFrom'),
            ageTo: getNumberValue('filterAgeTo'),
            createdFrom: document.getElementById('createdFrom').value || null,
            createdTo: document.getElementById('createdTo').value || null,
            page: currentPage,
            size: pageSize,
        };

        const result = await post('/detection-cases/search', filters);

        searchResults = result.content;
        totalPages = result.totalPages;

        renderSearchResults();
        renderPagination(result);

    } catch (error) {
        showToast('Ошибка поиска: ' + error.message, 'error');
        console.error(error);
    }
}

function getSelectValue(id) {
    const value = document.getElementById(id)?.value;
    return value ? parseInt(value) : null;
}

function getNumberValue(id) {
    const value = document.getElementById(id)?.value;
    return value ? parseInt(value) : null;
}

function renderSearchResults() {
    const tbody = document.getElementById('searchResults');

    if (!searchResults || searchResults.length === 0) {
        showTableEmptyState('searchResults', 'Пациенты не найдены');
        return;
    }

    tbody.innerHTML = searchResults.map(item => `
        <tr data-id="${item.detectionCaseId}" data-patient-id="${item.patientId}" onclick="selectSearchRow(this)" ondblclick="goToPatientCard(this)">
            <td>${escapeHtml(item.lastName || '')}</td>
            <td>${escapeHtml(item.firstName || '')}</td>
            <td>${escapeHtml(item.middleName || '')}</td>
            <td>${escapeHtml(item.genderName || '')}</td>
            <td>${formatDate(item.birthDate)}</td>
            <td>${escapeHtml(item.stateName || '')}</td>
            <td>${escapeHtml(item.diagnosisName || '')}</td>
            <td>${formatDate(item.diagnosisDate)}</td>
            <td>${escapeHtml(item.doctorName || '')}</td>
            <td>${formatDateTime(item.createdAt)}</td>
        </tr>
    `).join('');

    updateActionButtons();
}

function showTableEmptyState(tbodyId, message) {
    const tbody = document.getElementById(tbodyId);
    tbody.innerHTML = `
        <tr>
            <td colspan="10">
                <div class="empty-state">
                    <div class="empty-state-icon" style="display:none"></div>
                    <p>${message}</p>
                </div>
            </td>
        </tr>
    `;
}

function selectSearchRow(row) {
    document.querySelectorAll('#searchResults tr').forEach(tr => {
        tr.classList.remove('selected');
    });
    row.classList.add('selected');
    updateActionButtons();
}

// Переход на карточку пациента.
// Перед переходом сохраняем состояние поиска в sessionStorage.
function goToPatientCard(row) {
    const patientId = row.dataset.patientId;
    // Сохраняем фильтры и результаты — они восстановятся при возврате
    saveSearchState();
    window.location.href = `patient-card.html?id=${patientId}`;
}

function updateActionButtons() {
    const selected = document.querySelector('#searchResults tr.selected');
    const viewBtn = document.getElementById('btnViewPatient');
    const exportBtn = document.getElementById('btnExportExcel');

    if (viewBtn) viewBtn.disabled = !selected;
    if (exportBtn) exportBtn.disabled = !searchResults || searchResults.length === 0;
}

function renderPagination(pageData) {
    const container = document.getElementById('pagination');
    if (!container) return;

    const totalElements = pageData.totalElements || 0;
    const from = totalElements > 0 ? (currentPage * pageSize) + 1 : 0;
    const to = Math.min((currentPage + 1) * pageSize, totalElements);

    container.innerHTML = `
        <div class="pagination">
            <div class="pagination-info">Показано ${from} - ${to} из ${totalElements}</div>
            <div class="pagination-buttons">
                <button class="btn btn-secondary btn-sm" onclick="goToPage(0)" ${currentPage === 0 ? 'disabled' : ''}>««</button>
                <button class="btn btn-secondary btn-sm" onclick="goToPage(${currentPage - 1})" ${currentPage === 0 ? 'disabled' : ''}>«</button>
                <span style="padding: 6px 12px;">Страница ${currentPage + 1} из ${totalPages || 1}</span>
                <button class="btn btn-secondary btn-sm" onclick="goToPage(${currentPage + 1})" ${currentPage >= totalPages - 1 ? 'disabled' : ''}>»</button>
                <button class="btn btn-secondary btn-sm" onclick="goToPage(${totalPages - 1})" ${currentPage >= totalPages - 1 ? 'disabled' : ''}>»»</button>
            </div>
        </div>
    `;
}

function goToPage(page) {
    if (page < 0 || page >= totalPages) return;
    currentPage = page;
    searchPatients(false);
}

function resetFilters() {
    // Очищаем сохранённое состояние из sessionStorage
    sessionStorage.removeItem(SEARCH_STATE_KEY);

    document.getElementById('filterLastName').value = '';
    document.getElementById('filterFirstName').value = '';
    document.getElementById('filterMiddleName').value = '';
    document.getElementById('filterGender').value = '';
    document.getElementById('filterState').value = '';
    document.getElementById('filterDiagnosis').value = '';
    document.getElementById('filterDiagnosisGroup').value = '';
    document.getElementById('filterDoctor').value = '';
    document.getElementById('filterSocialGroup').value = '';
    document.getElementById('filterAgeFrom').value = '';
    document.getElementById('filterAgeTo').value = '';

    const today = new Date();
    const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);
    document.getElementById('createdFrom').value = firstDay.toISOString().split('T')[0];
    document.getElementById('createdTo').value = today.toISOString().split('T')[0];

    currentPage = 0;
    searchResults = [];
    totalPages = 0;

    document.getElementById('searchResults').innerHTML = `
        <tr>
            <td colspan="10">
                <div class="empty-state">
                    <div class="empty-state-icon" style="display:none"></div>
                    <p>Введите параметры поиска и нажмите "Поиск"</p>
                </div>
            </td>
        </tr>
    `;
    document.getElementById('pagination').innerHTML = '';

    showToast('Фильтры сброшены', 'info');
}

function viewPatientDetails() {
    const selected = document.querySelector('#searchResults tr.selected');
    if (!selected) {
        showToast('Выберите запись', 'info');
        return;
    }

    // Сохраняем состояние перед переходом
    saveSearchState();
    window.location.href = `patient-card.html?id=${selected.dataset.patientId}`;
}

async function exportToExcel() {
    try {
        const filters = {
            lastName: document.getElementById('filterLastName').value.trim() || null,
            firstName: document.getElementById('filterFirstName').value.trim() || null,
            middleName: document.getElementById('filterMiddleName').value.trim() || null,
            genderId: getSelectValue('filterGender'),
            stateId: getSelectValue('filterState'),
            diagnosisId: getSelectValue('filterDiagnosis'),
            diagnosisGroupId: getSelectValue('filterDiagnosisGroup'),
            doctorId: getSelectValue('filterDoctor'),
            socialGroupId: getSelectValue('filterSocialGroup'),
            ageFrom: getNumberValue('filterAgeFrom'),
            ageTo: getNumberValue('filterAgeTo'),
            createdFrom: document.getElementById('createdFrom').value || null,
            createdTo: document.getElementById('createdTo').value || null,
        };

        showToast('Формирование файла...', 'info');

        const csrfToken = getCsrfToken();
        const headers = {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        };
        if (csrfToken) headers['X-XSRF-TOKEN'] = csrfToken;

        const response = await fetch('/api/detection-cases/export', {
            method: 'POST',
            headers: headers,
            body: JSON.stringify(filters)
        });

        if (!response.ok) throw new Error('Ошибка экспорта');

        const blob = await response.blob();
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'search-results.xlsx';
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);

        showToast('Файл скачан', 'success');

    } catch (error) {
        showToast('Ошибка экспорта: ' + error.message, 'error');
        console.error(error);
    }
}

document.addEventListener('keydown', (e) => {
    if (e.key === 'Enter' && e.target.closest('#filtersCard')) {
        e.preventDefault();
        searchPatients();
    }
});
