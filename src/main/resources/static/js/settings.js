/**
 * KVD Application - Settings (Настройки)
 *
 * Функционал:
 * 1) Отображение информации о текущем пользователе (логин, роль)
 * 2) Смена собственного пароля (POST /api/auth/change-password)
 */

document.addEventListener('DOMContentLoaded', () => {
    loadUserInfo();
});

/**
 * Загружает информацию о текущем пользователе.
 * Использует тот же эндпоинт GET /api/auth/me, что и common.js для сайдбара.
 */
async function loadUserInfo() {
    try {
        const user = await get('/auth/me');

        document.getElementById('settingsUsername').textContent = user.username || '—';

        const roleNames = {
            'ADMIN': 'Администратор',
            'OPERATOR': 'Оператор'
        };
        document.getElementById('settingsRole').textContent = roleNames[user.role] || user.role;

        // Показываем секцию бэкапа только для ADMIN
        if (user.role === 'ADMIN') {
            const backupSection = document.getElementById('backupSection');
            if (backupSection) backupSection.style.display = '';
        }

    } catch (error) {
        console.error('Ошибка загрузки пользователя:', error);
    }
}

/**
 * Смена пароля.
 *
 * Валидация на фронте:
 * 1) Все три поля заполнены
 * 2) Новый пароль минимум 4 символа
 * 3) Новый пароль и подтверждение совпадают
 *
 * Бэкенд дополнительно проверяет:
 * - Старый пароль правильный (иначе 400 Bad Request)
 */
async function changePassword() {
    const oldPassword = document.getElementById('oldPassword').value;
    const newPassword = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;

    // Валидация
    if (!oldPassword || !newPassword || !confirmPassword) {
        showToast('Заполните все поля', 'error');
        return;
    }

    if (newPassword.length < 4) {
        showToast('Новый пароль должен быть не менее 4 символов', 'error');
        return;
    }

    if (newPassword !== confirmPassword) {
        showToast('Новый пароль и подтверждение не совпадают', 'error');
        return;
    }

    try {
        await post('/auth/change-password', {
            oldPassword: oldPassword,
            newPassword: newPassword
        });

        showToast('Пароль успешно изменён', 'success');

        // Очищаем поля после успешной смены
        document.getElementById('oldPassword').value = '';
        document.getElementById('newPassword').value = '';
        document.getElementById('confirmPassword').value = '';

    } catch (error) {
        showToast(error.message || 'Ошибка смены пароля', 'error');
    }
}

/**
 * Скачивание бэкапа БД.
 *
 * POST /api/backup → бэкенд выполняет pg_dump → возвращает .sql файл.
 * Используем fetch напрямую (не через post() из common.js),
 * потому что ответ — бинарный файл, а не JSON.
 */
async function downloadBackup() {
    const btn = document.getElementById('btnBackup');
    const originalText = btn.innerHTML;

    try {
        btn.disabled = true;
        btn.innerHTML = '⏳ Создание бэкапа...';

        const csrfToken = getCsrfToken();
        const headers = {
            'X-Requested-With': 'XMLHttpRequest'
        };
        if (csrfToken) {
            headers['X-XSRF-TOKEN'] = csrfToken;
        }

        const response = await fetch('/api/backup', {
            method: 'POST',
            headers: headers
        });

        if (!response.ok) {
            const error = await response.json().catch(() => ({ message: 'Ошибка сервера' }));
            throw new Error(error.message || 'Ошибка создания бэкапа');
        }

        // Получаем имя файла из заголовка Content-Disposition
        const disposition = response.headers.get('Content-Disposition');
        let fileName = 'kvd_backup.sql';
        if (disposition && disposition.includes('filename=')) {
            fileName = disposition.split('filename=')[1].replace(/"/g, '').trim();
        }

        // Скачиваем файл
        const blob = await response.blob();
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = fileName;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);

        showToast('Бэкап скачан: ' + fileName, 'success');

    } catch (error) {
        showToast('Ошибка: ' + error.message, 'error');
        console.error(error);
    } finally {
        btn.disabled = false;
        btn.innerHTML = originalText;
    }
}
