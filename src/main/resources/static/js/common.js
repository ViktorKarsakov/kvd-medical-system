/**
 * KVD Application - Common Utilities
 *
 * Этот файл подключается на КАЖДОЙ странице (кроме login.html).
 * Содержит общие функции: HTTP-запросы, уведомления, модалки, сайдбар.
 *
 * ИЗМЕНЕНИЯ для Spring Security:
 * ─────────────────────────────
 * 1) Добавлена функция getCsrfToken() — читает CSRF-токен из cookie.
 *    Токен нужно отправлять с каждым POST/PUT/DELETE запросом,
 *    иначе Spring Security отклонит запрос (защита от CSRF-атак).
 *
 * 2) fetchApi() обновлён: добавляет CSRF-заголовок и обрабатывает 401.
 *    401 Unauthorized = сессия истекла → перенаправляем на логин.
 *
 * 3) Добавлена функция loadCurrentUser() — загружает имя и роль
 *    текущего пользователя для отображения в сайдбаре.
 *
 * 4) Добавлена функция logout() — отправляет POST /logout.
 */

const API_BASE = '/api';

// ==================== CSRF-ТОКЕН ====================

/**
 * Читает CSRF-токен из cookie "XSRF-TOKEN".
 *
 * Как работает CSRF-защита в нашем приложении:
 * ─────────────────────────────────────────────
 * 1) Spring Security при каждом ответе ставит cookie "XSRF-TOKEN" со случайным значением.
 * 2) Наш JavaScript читает это значение из cookie.
 * 3) При каждом POST/PUT/DELETE запросе мы добавляем заголовок "X-XSRF-TOKEN" с этим значением.
 * 4) Spring Security сравнивает значение cookie и заголовка — если совпадают, запрос легитимный.
 *
 * Почему это работает:
 * Вредоносный сайт может заставить браузер ОТПРАВИТЬ cookie (автоматически),
 * но НЕ МОЖЕТ ПРОЧИТАТЬ cookie другого домена (Same-Origin Policy).
 * Поэтому вредоносный сайт не сможет добавить правильный заголовок X-XSRF-TOKEN.
 *
 * @returns {string|null} — значение CSRF-токена или null, если cookie не найдена
 */
function getCsrfToken() {
    const cookies = document.cookie.split(';');
    for (const cookie of cookies) {
        const [name, value] = cookie.trim().split('=');
        if (name === 'XSRF-TOKEN') {
            // decodeURIComponent — потому что cookie-значение может быть URL-encoded
            return decodeURIComponent(value);
        }
    }
    return null;
}

// ==================== HTTP UTILS ====================

/**
 * Базовая функция для всех HTTP-запросов к API.
 *
 * Что делает:
 * 1) Добавляет Content-Type: application/json
 * 2) Добавляет CSRF-токен для POST/PUT/DELETE (Spring Security требует)
 * 3) Проверяет статус ответа
 * 4) При 401 (сессия истекла) — перенаправляет на страницу входа
 * 5) Парсит JSON-ответ
 */
async function fetchApi(endpoint, options = {}) {

    // Базовые заголовки
    const headers = {
        'Content-Type': 'application/json',
        // X-Requested-With — маркер AJAX-запроса.
        // SecurityConfig проверяет этот заголовок, чтобы отличать AJAX от обычных переходов.
        // Для AJAX возвращает 401 (JavaScript обработает), для обычных — редирект на login.
        'X-Requested-With': 'XMLHttpRequest',
        // Если в options переданы дополнительные заголовки — они дополнят/перезапишут эти
        ...(options.headers || {})
    };

    // Для запросов, ИЗМЕНЯЮЩИХ данные (POST, PUT, DELETE),
    // добавляем CSRF-токен в заголовок.
    // GET-запросы не нуждаются в CSRF — они не изменяют данные.
    const method = (options.method || 'GET').toUpperCase();
    if (method !== 'GET') {
        const csrfToken = getCsrfToken();
        if (csrfToken) {
            headers['X-XSRF-TOKEN'] = csrfToken;
        }
    }

    const response = await fetch(`${API_BASE}${endpoint}`, {
        ...options,
        headers: headers,
    });

    // ── Обработка 401 Unauthorized ──
    // Сессия истекла (пользователь давно не работал) или не авторизован.
    // Перенаправляем на страницу входа.
    if (response.status === 401) {
        window.location.href = '/login.html';
        return;
    }

    // ── Обработка 403 Forbidden ──
    // Пользователь авторизован, но нет прав (оператор пытается удалить справочник).
    if (response.status === 403) {
        throw new Error('Недостаточно прав для выполнения операции');
    }

    if (!response.ok) {
        const error = await response.json().catch(() => ({ message: 'Ошибка сервера' }));
        throw new Error(error.message || `HTTP ${response.status}`);
    }

    // Для DELETE запросов нет тела ответа
    if (response.status === 204) {
        return null;
    }

    return response.json();
}

async function get(endpoint) {
    return fetchApi(endpoint);
}

async function post(endpoint, data) {
    return fetchApi(endpoint, {
        method: 'POST',
        body: JSON.stringify(data),
    });
}

async function put(endpoint, data) {
    return fetchApi(endpoint, {
        method: 'PUT',
        body: JSON.stringify(data),
    });
}

async function del(endpoint) {
    return fetchApi(endpoint, {
        method: 'DELETE',
    });
}

// ==================== АУТЕНТИФИКАЦИЯ ====================

/**
 * Загружает информацию о текущем пользователе и отображает в сайдбаре.
 *
 * Вызывается при загрузке каждой страницы (автоматически — см. вызов внизу этого файла).
 *
 * Запрашивает GET /api/auth/me → { username: "admin", role: "ADMIN" }
 *
 * Обновляет:
 * 1) Имя пользователя в сайдбаре (элемент #currentUserName)
 * 2) Глобальную переменную currentUserRole — для проверки роли в других JS-файлах
 */
let currentUserRole = null;

async function loadCurrentUser() {
    try {
        const user = await get('/auth/me');

        // Показываем имя в сайдбаре
        const nameEl = document.getElementById('currentUserName');
        if (nameEl) {
            nameEl.textContent = user.username;
        }

        // Сохраняем роль глобально.
        // Другие скрипты (dictionaries.js) смогут проверять:
        //   if (currentUserRole !== 'ADMIN') { скрыть кнопку "Удалить" }
        currentUserRole = user.role;

        // Показываем пункт "Пользователи" в сайдбаре только для ADMIN
        const navUsers = document.getElementById('navUsers');
        if (navUsers) {
            navUsers.style.display = (user.role === 'ADMIN') ? '' : 'none';
        }

        // Показываем пункт "Журнал действий" в сайдбаре только для ADMIN
        const navAuditLog = document.getElementById('navAuditLog');
        if (navAuditLog) {
            navAuditLog.style.display = (user.role === 'ADMIN') ? '' : 'none';
        }

    } catch (error) {
        console.error('Не удалось загрузить пользователя:', error);
    }
}

/**
 * Выход из системы.
 *
 * Отправляет POST /logout (Spring Security обрабатывает автоматически):
 * 1) Удаляет серверную сессию
 * 2) Удаляет cookie JSESSIONID
 * 3) Возвращает редирект на /login.html?logout
 *
 * Мы используем fetch вместо обычной формы, потому что:
 * - Нужен CSRF-токен в заголовке
 * - POST /logout (Spring Security по умолчанию требует POST, не GET)
 */
async function logout() {
    try {
        const csrfToken = getCsrfToken();
        const headers = {};
        if (csrfToken) {
            headers['X-XSRF-TOKEN'] = csrfToken;
        }

        await fetch('/logout', {
            method: 'POST',
            headers: headers
        });
    } catch (e) {
        // Даже при ошибке сети — отправляем на login
    }
    window.location.href = '/login.html?logout';
}

// ==================== TOAST NOTIFICATIONS ====================

function showToast(message, type = 'info') {
    let container = document.querySelector('.toast-container');
    if (!container) {
        container = document.createElement('div');
        container.className = 'toast-container';
        document.body.appendChild(container);
    }

    const icons = {
        success: '✓',
        error: '✕',
        info: 'ℹ',
    };

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `<span>${icons[type] || ''}</span> ${message}`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.animation = 'toastIn 0.3s ease reverse';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

// ==================== MODAL UTILS ====================

function openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.add('open');
    }
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.remove('open');
    }
}

// Закрытие модала по клику на overlay
document.addEventListener('click', (e) => {
    if (e.target.classList.contains('modal-overlay')) {
        e.target.classList.remove('open');
    }
});

// Закрытие модала по Escape
document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
        document.querySelectorAll('.modal-overlay.open').forEach(modal => {
            modal.classList.remove('open');
        });
    }
});

// ==================== FORM UTILS ====================

function getFormData(formId) {
    const form = document.getElementById(formId);
    const formData = new FormData(form);
    const data = {};

    for (const [key, value] of formData.entries()) {
        if (value !== '') {
            data[key] = value;
        }
    }

    return data;
}

function setFormData(formId, data) {
    const form = document.getElementById(formId);
    
    Object.keys(data).forEach(key => {
        const input = form.querySelector(`[name="${key}"]`);
        if (input) {
            input.value = data[key] ?? '';
        }
    });
}

function clearForm(formId) {
    const form = document.getElementById(formId);
    if (form) {
        form.reset();
    }
}

// ==================== SELECT UTILS ====================

async function loadSelectOptions(selectId, endpoint, displayField = 'name', valueField = 'id', emptyLabel = 'Выберите...') {
    const select = document.getElementById(selectId);
    if (!select) return;

    try {
        const data = await get(endpoint);
        
        // Сохраняем текущее значение
        const currentValue = select.value;
        
        // Очищаем и добавляем пустой option
        select.innerHTML = `<option value="">${emptyLabel}</option>`;
        
        data.forEach(item => {
            const option = document.createElement('option');
            option.value = item[valueField];
            
            // Для вложенных объектов (например, doctor.department.name)
            if (displayField.includes('.')) {
                const fields = displayField.split('.');
                let value = item;
                for (const field of fields) {
                    value = value?.[field];
                }
                option.textContent = value || '';
            } else {
                option.textContent = item[displayField] || '';
            }
            
            select.appendChild(option);
        });
        
        // Восстанавливаем значение
        if (currentValue) {
            select.value = currentValue;
        }
    } catch (error) {
        console.error(`Error loading options for ${selectId}:`, error);
    }
}

// ==================== DATE UTILS ====================

function formatDate(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('ru-RU');
}

function formatDateTime(dateTimeString) {
    if (!dateTimeString) return '';
    const date = new Date(dateTimeString);
    return date.toLocaleString('ru-RU');
}

function toISODate(dateString) {
    if (!dateString) return null;
    const date = new Date(dateString);
    return date.toISOString().split('T')[0];
}

// ==================== TABLE UTILS ====================

function selectRow(row, tableId) {
    // Убираем выделение с других строк
    document.querySelectorAll(`#${tableId} tbody tr`).forEach(tr => {
        tr.classList.remove('selected');
    });
    
    // Выделяем текущую
    row.classList.add('selected');
}

function getSelectedRowId(tableId) {
    const selected = document.querySelector(`#${tableId} tbody tr.selected`);
    return selected ? selected.dataset.id : null;
}

function clearTableSelection(tableId) {
    document.querySelectorAll(`#${tableId} tbody tr`).forEach(tr => {
        tr.classList.remove('selected');
    });
}

// ==================== VALIDATION ====================

function validateRequired(value, fieldName) {
    if (!value || value.toString().trim() === '') {
        throw new Error(`Поле "${fieldName}" обязательно для заполнения`);
    }
}

function validateForm(rules) {
    for (const [fieldName, { value, label }] of Object.entries(rules)) {
        validateRequired(value, label);
    }
}

// ==================== SIDEBAR ====================

function toggleSubmenu(element) {
    element.classList.toggle('collapsed');
    const submenu = element.nextElementSibling;
    if (submenu && submenu.classList.contains('submenu')) {
        submenu.classList.toggle('collapsed');
    }
}

// Подсветка активного пункта меню
function setActiveMenuItem(page) {
    document.querySelectorAll('.sidebar-nav a').forEach(a => {
        a.classList.remove('active');
    });
    
    const activeLink = document.querySelector(`.sidebar-nav a[href="${page}"]`);
    if (activeLink) {
        activeLink.classList.add('active');
        
        // Если это подменю, раскрываем его
        const submenu = activeLink.closest('.submenu');
        if (submenu) {
            submenu.classList.remove('collapsed');
            const toggle = submenu.previousElementSibling;
            if (toggle) {
                toggle.classList.remove('collapsed');
            }
        }
    }
}

// ==================== CONFIRM DIALOG ====================

function confirmAction(message) {
    return confirm(message);
}

// ==================== LOADING STATE ====================

function showLoading(containerId) {
    const container = document.getElementById(containerId);
    if (container) {
        container.innerHTML = `
            <div class="loading">
                <div class="spinner"></div>
            </div>
        `;
    }
}

function showEmptyState(containerId, message = 'Нет данных') {
    const container = document.getElementById(containerId);
    if (container) {
        container.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">📋</div>
                <p>${message}</p>
            </div>
        `;
    }
}

// ==================== HTML UTILS ====================

/**
 * Экранирование HTML — защита от XSS.
 * Используется при вставке пользовательских данных в DOM.
 */
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// ==================== АВТОЗАГРУЗКА ПОЛЬЗОВАТЕЛЯ ====================

/**
 * При загрузке любой страницы (кроме login) автоматически загружаем
 * информацию о текущем пользователе.
 *
 * Если пользователь не авторизован — fetchApi получит 401 → редирект на login.
 */
document.addEventListener('DOMContentLoaded', () => {
    loadCurrentUser();
});
