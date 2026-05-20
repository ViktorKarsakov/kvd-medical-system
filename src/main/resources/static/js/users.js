/**
 * users.js — логика страницы управления пользователями.
 * Доступна только админу.
 */

document.addEventListener('DOMContentLoaded', () => {
    setActiveMenuItem('users.html');
    loadUsers();
});

async function loadUsers() {
    try {
        const users = await get('/users');
        renderUsersTable(users);
    } catch (error) {
        showToast('Ошибка загрузки пользователей: ' + error.message, 'error');
    }
}

function renderUsersTable(users) {
    const tbody = document.getElementById('usersTableBody');
    if (!users || users.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" style="text-align:center; color:#6b7280;">Нет пользователей</td></tr>';
        return;
    }

    tbody.innerHTML = users.map(user => {
        const roleLabel = user.role === 'ADMIN' ? 'Администратор' : 'Оператор';
        const statusBadge = user.enabled
            ? '<span style="color:#16a34a; font-weight:600;">Активен</span>'
            : '<span style="color:#dc2626; font-weight:600;">Заблокирован</span>';
        const createdAt = user.createdAt ? new Date(user.createdAt).toLocaleDateString('ru-RU') : '—';

        const toggleBtn = user.enabled
            ? `<button class="btn btn-small btn-danger" onclick="toggleUser(${user.id}, false)" title="Заблокировать">🔒</button>`
            : `<button class="btn btn-small btn-success" onclick="toggleUser(${user.id}, true)" title="Разблокировать">🔓</button>`;

        const resetBtn = `<button class="btn btn-small" onclick="openResetModal(${user.id}, '${user.username}')" title="Сбросить пароль">🔑</button>`;

        return `
            <tr>
                <td>${user.username}</td>
                <td>${user.fullName}</td>
                <td>${roleLabel}</td>
                <td>${statusBadge}</td>
                <td>${createdAt}</td>
                <td style="display:flex; gap:6px;">
                    ${resetBtn}
                    ${toggleBtn}
                </td>
            </tr>
        `;
    }).join('');
}

function openCreateModal() {
    clearForm('createUserForm');
    openModal('createUserModal');
}

async function createUser() {
    const form = document.getElementById('createUserForm');
    const data = {
        username: form.querySelector('[name="username"]').value.trim(),
        password: form.querySelector('[name="password"]').value,
        fullName: form.querySelector('[name="fullName"]').value.trim(),
        role: form.querySelector('[name="role"]').value
    };

    if (!data.username || !data.password || !data.fullName) {
        showToast('Заполните все обязательные поля', 'error');
        return;
    }

    if (data.password.length < 4) {
        showToast('Пароль должен быть не менее 4 символов', 'error');
        return;
    }

    try {
        await post('/users', data);
        closeModal('createUserModal');
        showToast('Пользователь создан', 'success');
        loadUsers();
    } catch (error) {
        showToast('Ошибка: ' + error.message, 'error');
    }
}

async function toggleUser(userId, enabled) {
    const action = enabled ? 'разблокировать' : 'заблокировать';
    if (!confirmAction(`Вы уверены, что хотите ${action} пользователя?`)) return;

    try {
        await fetchApi(`/users/${userId}/toggle`, {
            method: 'PATCH',
            body: JSON.stringify({ enabled: enabled })
        });
        showToast(enabled ? 'Пользователь разблокирован' : 'Пользователь заблокирован', 'success');
        loadUsers();
    } catch (error) {
        showToast('Ошибка: ' + error.message, 'error');
    }
}

function openResetModal(userId, username) {
    document.getElementById('resetUserId').value = userId;
    document.getElementById('resetPasswordInfo').textContent = `Сброс пароля для пользователя: ${username}`;
    document.getElementById('resetPasswordForm').querySelector('[name="newPassword"]').value = '';
    openModal('resetPasswordModal');
}

async function resetPassword() {
    const userId = document.getElementById('resetUserId').value;
    const newPassword = document.getElementById('resetPasswordForm').querySelector('[name="newPassword"]').value;

    if (!newPassword || newPassword.length < 4) {
        showToast('Пароль должен быть не менее 4 символов', 'error');
        return;
    }

    try {
        await fetchApi(`/users/${userId}/reset-password`, {
            method: 'PATCH',
            body: JSON.stringify({ newPassword: newPassword })
        });
        closeModal('resetPasswordModal');
        showToast('Пароль сброшен', 'success');
    } catch (error) {
        showToast('Ошибка: ' + error.message, 'error');
    }
}
