/**
 * KVD Application - Patient Search
 */

let searchResults = [];
let currentPage = 0;
let totalPages = 0;
let pageSize = 20;

document.addEventListener('DOMContentLoaded', () => {
    initSearchPage();
});

async function initSearchPage() {
    // Загружаем справочники для фильтров
    await loadSearchFilters();
    
    // Устанавливаем даты по умолчанию (текущий месяц)
    const today = new Date();
    const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);
    
    document.getElementById('createdFrom').value = firstDay.toISOString().split('T')[0];
    document.getElementById('createdTo').value = today.toISOString().split('T')[0];
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
        
        // Врачи загружаются отдельно (особый формат ФИО)
        await loadDoctorsSelectForSearch();
    } catch (error) {
        console.error('Error loading filters:', error);
    }
}

// Загрузка врачей для фильтра поиска
async function loadDoctorsSelectForSearch() {
    const select = document.getElementById('filterDoctor');
    if (!select) return;
    
    try {
        const doctors = await get('/dictionaries/doctors');
        select.innerHTML = '<option value="">Любой</option>';
        
        doctors.forEach(doctor => {
            const option = document.createElement('option');
            option.value = doctor.id;
            
            // Форматируем: Иванов И.С.
            let fullName = doctor.lastName || '';
            if (doctor.firstName) {
                fullName += ` ${doctor.firstName.charAt(0)}.`;
            }
            if (doctor.middleName) {
                fullName += `${doctor.middleName.charAt(0)}.`;
            }
            
            option.textContent = fullName;
            select.appendChild(option);
        });
    } catch (error) {
        console.error('Error loading doctors:', error);
    }
}

async function searchPatients(resetPage = true) {
    try {
        // При новом поиске сбрасываем на первую страницу
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
                    <div class="empty-state-icon">📋</div>
                    <p>${message}</p>
                </div>
            </td>
        </tr>
    `;
}

function selectSearchRow(row) {
    // Убираем выделение с других строк
    document.querySelectorAll('#searchResults tr').forEach(tr => {
        tr.classList.remove('selected');
    });
    
    // Выделяем текущую
    row.classList.add('selected');
    
    updateActionButtons();
}

// Переход на карточку по двойному клику
function goToPatientCard(row) {
    const patientId = row.dataset.patientId;
    window.location.href = `patient-card.html?id=${patientId}`;
}

function updateActionButtons() {
    const selected = document.querySelector('#searchResults tr.selected');
    const viewBtn = document.getElementById('btnViewPatient');
    const exportBtn = document.getElementById('btnExportExcel');
    
    if (viewBtn) {
        viewBtn.disabled = !selected;
    }
    // Кнопка экспорта активна если есть результаты поиска
    if (exportBtn) {
        exportBtn.disabled = !searchResults || searchResults.length === 0;
    }
}

function renderPagination(pageData) {
    const container = document.getElementById('pagination');
    if (!container) return;
    
    const totalElements = pageData.totalElements || 0;
    const from = totalElements > 0 ? (currentPage * pageSize) + 1 : 0;
    const to = Math.min((currentPage + 1) * pageSize, totalElements);
    
    container.innerHTML = `
        <div class="pagination">
            <div class="pagination-info">
                Показано ${from} - ${to} из ${totalElements}
            </div>
            <div class="pagination-buttons">
                <button class="btn btn-secondary btn-sm" onclick="goToPage(0)" ${currentPage === 0 ? 'disabled' : ''}>
                    ««
                </button>
                <button class="btn btn-secondary btn-sm" onclick="goToPage(${currentPage - 1})" ${currentPage === 0 ? 'disabled' : ''}>
                    «
                </button>
                <span style="padding: 6px 12px;">Страница ${currentPage + 1} из ${totalPages || 1}</span>
                <button class="btn btn-secondary btn-sm" onclick="goToPage(${currentPage + 1})" ${currentPage >= totalPages - 1 ? 'disabled' : ''}>
                    »
                </button>
                <button class="btn btn-secondary btn-sm" onclick="goToPage(${totalPages - 1})" ${currentPage >= totalPages - 1 ? 'disabled' : ''}>
                    »»
                </button>
            </div>
        </div>
    `;
}

function goToPage(page) {
    if (page < 0 || page >= totalPages) return;
    currentPage = page;
    searchPatients(false); // false = не сбрасывать страницу
}

function resetFilters() {
    // Очищаем все фильтры
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
    
    // Сбрасываем даты на текущий месяц
    const today = new Date();
    const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);
    document.getElementById('createdFrom').value = firstDay.toISOString().split('T')[0];
    document.getElementById('createdTo').value = today.toISOString().split('T')[0];
    
    // Очищаем результаты и показываем подсказку
    currentPage = 0;
    searchResults = [];
    totalPages = 0;
    
    document.getElementById('searchResults').innerHTML = `
        <tr>
            <td colspan="10">
                <div class="empty-state">
                    <div class="empty-state-icon">🔍</div>
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
    
    const patientId = selected.dataset.patientId;
    
    // Переходим на карточку пациента
    window.location.href = `patient-card.html?id=${patientId}`;
}

/**
 * Экспорт результатов поиска в Excel.
 *
 * Отправляет те же фильтры, что и обычный поиск, но на эндпоинт /export.
 * Бэкенд возвращает .xlsx файл, фронт скачивает через Blob.
 */
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

        // Отправляем запрос напрямую через fetch (не через post() из common.js,
        // потому что ответ — бинарный файл, а не JSON)
        const csrfToken = getCsrfToken();
        const headers = {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        };
        if (csrfToken) {
            headers['X-XSRF-TOKEN'] = csrfToken;
        }

        const response = await fetch('/api/detection-cases/export', {
            method: 'POST',
            headers: headers,
            body: JSON.stringify(filters)
        });

        if (!response.ok) {
            throw new Error('Ошибка экспорта');
        }

        // Получаем файл как Blob и скачиваем
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

// Поиск по Enter только в блоке фильтров
document.addEventListener('keydown', (e) => {
    if (e.key === 'Enter' && e.target.closest('#filtersCard')) {
        e.preventDefault();
        searchPatients();
    }
});
