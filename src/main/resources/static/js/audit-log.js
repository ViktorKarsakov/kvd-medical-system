/**
 * KVD Application - Audit Log (Журнал действий)
 *
 * Загружает записи журнала с пагинацией и фильтрами.
 * Доступна только администратору.
 */

let currentPage = 0;
const PAGE_SIZE = 20;

document.addEventListener('DOMContentLoaded', () => {
    loadAuditLog();
});

async function loadAuditLog() {
    const entityType = document.getElementById('filterEntityType').value;
    const username = document.getElementById('filterUsername').value.trim();

    // Формируем параметры запроса
    let url = `/audit-log?page=${currentPage}&size=${PAGE_SIZE}`;
    if (entityType) url += `&entityType=${entityType}`;
    if (username) url += `&username=${encodeURIComponent(username)}`;

    try {
        const result = await get(url);
        renderTable(result.content || []);
        renderPagination(result);
    } catch (error) {
        console.error('Ошибка загрузки журнала:', error);
        showToast('Ошибка загрузки журнала', 'error');
    }
}

function renderTable(entries) {
    const tbody = document.getElementById('auditTableBody');

    if (entries.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5">
            <div class="empty-state">
                <div class="empty-state-icon">📜</div>
                <p>Нет записей</p>
            </div>
        </td></tr>`;
        return;
    }

    tbody.innerHTML = entries.map(entry => {
        const actionBadge = getActionBadge(entry.action);
        const typeBadge = getTypeBadge(entry.entityType);

        return `
            <tr>
                <td>${formatDateTime(entry.createdAt)}</td>
                <td>${escapeHtml(entry.username)}</td>
                <td>${actionBadge}</td>
                <td>${typeBadge}</td>
                <td>${escapeHtml(entry.details)}</td>
            </tr>
        `;
    }).join('');
}

/**
 * Цветные бейджи для типа действия.
 */
function getActionBadge(action) {
    const map = {
        'CREATE': '<span class="badge badge-success">Создание</span>',
        'UPDATE': '<span class="badge badge-info">Изменение</span>',
        'DELETE': '<span class="badge badge-danger">Удаление</span>'
    };
    return map[action] || `<span class="badge">${escapeHtml(action)}</span>`;
}

/**
 * Бейджи для типа сущности.
 */
function getTypeBadge(type) {
    const map = {
        'PATIENT': 'Пациент',
        'CASE': 'Случай',
        'DICTIONARY': 'Справочник',
        'USER': 'Пользователь'
    };
    return map[type] || type;
}

/**
 * Пагинация: кнопки "Назад" / "Вперёд" + информация о текущей странице.
 */
function renderPagination(pageData) {
    const container = document.getElementById('pagination');

    if (pageData.totalPages <= 1) {
        container.innerHTML = '';
        return;
    }

    container.innerHTML = `
        <button class="btn btn-secondary btn-sm" 
                ${pageData.first ? 'disabled' : ''} 
                onclick="goToPage(${currentPage - 1})">← Назад</button>
        <span class="pagination-info">
            Страница ${pageData.number + 1} из ${pageData.totalPages}
            (всего ${pageData.totalElements})
        </span>
        <button class="btn btn-secondary btn-sm" 
                ${pageData.last ? 'disabled' : ''} 
                onclick="goToPage(${currentPage + 1})">Вперёд →</button>
    `;
}

function goToPage(page) {
    currentPage = page;
    loadAuditLog();
}

/**
 * При применении фильтров сбрасываем на первую страницу.
 */
const filterEntityType = document.getElementById('filterEntityType');
const filterUsername = document.getElementById('filterUsername');

if (filterEntityType) {
    filterEntityType.addEventListener('change', () => {
        currentPage = 0;
        loadAuditLog();
    });
}
