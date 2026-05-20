/**
 * KVD Application - Patient Card
 */

let patientId = null;
let patientData = null;
let labTests = [];
let editingCaseId = null;

document.addEventListener('DOMContentLoaded', () => {
    initPatientCard();
});

async function initPatientCard() {
    // Получаем ID пациента из URL
    const params = new URLSearchParams(window.location.search);
    patientId = params.get('id');
    
    if (!patientId) {
        showToast('ID пациента не указан', 'error');
        setTimeout(() => {
            window.location.href = 'patient-search.html';
        }, 1500);
        return;
    }
    
    // Загружаем лаб. тесты для модалки
    await loadLabTests();
    
    // Загружаем справочники для модалок
    await loadModalDictionaries();
    
    // Загружаем данные пациента
    await loadPatient();
}

async function loadLabTests() {
    try {
        labTests = await get('/dictionaries/lab-test-types');
    } catch (error) {
        console.error('Error loading lab tests:', error);
    }
}

async function loadModalDictionaries() {
    try {
        await Promise.all([
            // Для редактирования пациента
            loadSelectOptions('editGender', '/dictionaries/genders', 'name', 'id', 'Выберите...'),
            // Для случая
            loadSelectOptions('caseCitizenCategory', '/dictionaries/citizen-categories', 'name', 'id', 'Выберите...'),
            loadSelectOptions('caseCitizenType', '/dictionaries/citizen-types', 'name', 'id', 'Выберите...'),
            loadSelectOptions('caseState', '/dictionaries/states', 'name', 'id', 'Выберите...'),
            loadSelectOptions('caseSocialGroup', '/dictionaries/social-groups', 'name', 'id', 'Выберите...'),
            loadSelectOptions('caseDiagnosis', '/dictionaries/diagnoses', 'name', 'id', 'Выберите...'),
            loadSelectOptions('casePlace', '/dictionaries/places', 'name', 'id', 'Выберите...'),
            loadSelectOptions('caseProfile', '/dictionaries/profiles', 'name', 'id', 'Выберите...'),
            loadSelectOptions('caseInspection', '/dictionaries/inspections', 'name', 'id', 'Выберите...'),
            loadSelectOptions('caseTransfer', '/dictionaries/transfers', 'name', 'id', 'Выберите...'),
        ]);
        
        // Врачи отдельно
        await loadDoctorsSelect('caseDoctor');
        
        // Рендерим чекбоксы лаб. тестов
        renderLabTestsCheckboxes();
    } catch (error) {
        console.error('Error loading dictionaries:', error);
    }
}

async function loadDoctorsSelect(selectId) {
    const select = document.getElementById(selectId);
    if (!select) return;
    
    try {
        const doctors = await get('/dictionaries/doctors');
        select.innerHTML = '<option value="">Выберите...</option>';
        
        doctors.forEach(doctor => {
            const option = document.createElement('option');
            option.value = doctor.id;
            
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

function renderLabTestsCheckboxes() {
    const container = document.getElementById('caseLabTests');
    if (!container || labTests.length === 0) return;
    
    container.innerHTML = labTests.map(test => `
        <label class="checkbox-item">
            <input type="checkbox" value="${test.id}" id="labTest_${test.id}">
            <span>${escapeHtml(test.name)}</span>
        </label>
    `).join('');
}

async function loadPatient() {
    try {
        patientData = await get(`/patients/${patientId}`);
        renderPatientInfo();
        renderCases();
    } catch (error) {
        console.error('Error loading patient:', error);
        document.getElementById('patientInfoCard').innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">❌</div>
                <p>Пациент не найден</p>
            </div>
        `;
        document.getElementById('casesContainer').innerHTML = '';
    }
}

function renderPatientInfo() {
    const p = patientData;
    const age = calculateAge(p.birthDate);
    const fullName = [p.lastName, p.firstName, p.middleName].filter(Boolean).join(' ');
    
    document.getElementById('patientInfoCard').innerHTML = `
        <div style="display: flex; justify-content: space-between; align-items: flex-start;">
            <div>
                <h2 class="card-title" style="margin-bottom: 8px;">👤 ${escapeHtml(fullName)}</h2>
                <div class="patient-meta">
                    <span class="badge badge-${p.gender?.name === 'Мужской' ? 'info' : 'pink'}">
                        ${escapeHtml(p.gender?.name || 'Пол не указан')}
                    </span>
                    <span style="margin-left: 12px; color: #6b7280;">
                        📅 ${formatDate(p.birthDate)} (${age} лет)
                    </span>
                    <span style="margin-left: 12px; color: #6b7280;">
                        📍 ${escapeHtml(p.address || 'Адрес не указан')}
                    </span>
                </div>
            </div>
            <div style="display: flex; gap: 8px;">
                <button class="btn btn-secondary btn-sm" onclick="openEditPatientModal()">
                    ✏️ Редактировать
                </button>
                <button class="btn btn-danger btn-sm" onclick="confirmDeletePatient()">
                    🗑️ Удалить
                </button>
            </div>
        </div>
    `;
}

function renderCases() {
    const cases = patientData.cases || [];
    const container = document.getElementById('casesContainer');
    
    if (cases.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">📋</div>
                <p>Нет записей о заболеваниях</p>
            </div>
        `;
        return;
    }
    
    container.innerHTML = cases.map(c => `
        <div class="case-card" data-id="${c.id}">
            <div class="case-header">
                <div>
                    <span class="case-diagnosis">${escapeHtml(c.diagnosis?.name || 'Без диагноза')}</span>
                    <span class="case-date">Диагноз: ${formatDate(c.diagnosisDate)}</span>
                </div>
                <div class="case-actions">
                    <button class="btn btn-secondary btn-sm" onclick="openEditCaseModal(${c.id})">✏️</button>
                    <button class="btn btn-danger btn-sm" onclick="confirmDeleteCase(${c.id})">🗑️</button>
                </div>
            </div>
            <div class="case-details">
                <div class="case-detail">
                    <span class="label">Врач</span>
                    <span class="value">${escapeHtml(c.doctor?.name || '—')}</span>
                </div>
                <div class="case-detail">
                    <span class="label">Место выявления</span>
                    <span class="value">${escapeHtml(c.place?.name || '—')}</span>
                </div>
                <div class="case-detail">
                    <span class="label">Профиль</span>
                    <span class="value">${escapeHtml(c.profile?.name || '—')}</span>
                </div>
                <div class="case-detail">
                    <span class="label">Осмотр</span>
                    <span class="value">${escapeHtml(c.inspection?.name || '—')}</span>
                </div>
                <div class="case-detail">
                    <span class="label">Путь передачи</span>
                    <span class="value">${escapeHtml(c.transfer?.name || '—')}</span>
                </div>
                <div class="case-detail">
                    <span class="label">Район</span>
                    <span class="value">${escapeHtml(c.state?.name || '—')}</span>
                </div>
                <div class="case-detail">
                    <span class="label">Категория</span>
                    <span class="value">${escapeHtml(c.citizenCategory?.name || '—')}</span>
                </div>
                <div class="case-detail">
                    <span class="label">Тип н.п.</span>
                    <span class="value">${escapeHtml(c.citizenType?.name || '—')}</span>
                </div>
                <div class="case-detail">
                    <span class="label">Соц. группа</span>
                    <span class="value">${escapeHtml(c.socialGroup?.name || '—')}</span>
                </div>
                <div class="case-detail">
                    <span class="label">По контакту</span>
                    <span class="value">${c.isContact ? '✓ Да' : 'Нет'}</span>
                </div>
            </div>
            <div class="case-footer">
                <span>Дата учёта: ${formatDateTime(c.createdAt)}</span>
                <span style="margin-left: 16px;">Оператор: ${escapeHtml(c.createdByUsername || '—')}</span>
            </div>
        </div>
    `).join('');
}

function calculateAge(birthDate) {
    if (!birthDate) return '—';
    const today = new Date();
    const birth = new Date(birthDate);
    let age = today.getFullYear() - birth.getFullYear();
    const monthDiff = today.getMonth() - birth.getMonth();
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) {
        age--;
    }
    return age;
}

// ==================== РЕДАКТИРОВАНИЕ ПАЦИЕНТА ====================

function openEditPatientModal() {
    const p = patientData;
    
    document.getElementById('editLastName').value = p.lastName || '';
    document.getElementById('editFirstName').value = p.firstName || '';
    document.getElementById('editMiddleName').value = p.middleName || '';
    document.getElementById('editGender').value = p.gender?.id || '';
    document.getElementById('editBirthDate').value = p.birthDate || '';
    document.getElementById('editAddress').value = p.address || '';
    
    openModal('editPatientModal');
    
    // Подсказки адреса через DaData
    initAddressAutocomplete('editAddress');
}

async function savePatient() {
    const data = {
        lastName: document.getElementById('editLastName').value.trim(),
        firstName: document.getElementById('editFirstName').value.trim(),
        middleName: document.getElementById('editMiddleName').value.trim() || null,
        genderId: parseInt(document.getElementById('editGender').value),
        birthDate: document.getElementById('editBirthDate').value,
        address: document.getElementById('editAddress').value.trim() || null
    };
    
    if (!data.lastName || !data.firstName || !data.genderId || !data.birthDate) {
        showToast('Заполните обязательные поля', 'error');
        return;
    }
    
    try {
        await put(`/patients/${patientId}`, data);
        showToast('Данные пациента обновлены', 'success');
        closeModal('editPatientModal');
        await loadPatient();
    } catch (error) {
        showToast('Ошибка сохранения: ' + error.message, 'error');
    }
}

// ==================== УДАЛЕНИЕ ПАЦИЕНТА ====================

function confirmDeletePatient() {
    document.getElementById('deleteConfirmText').textContent = 
        'Удалить пациента и все его записи о заболеваниях? Это действие нельзя отменить.';
    
    document.getElementById('deleteConfirmBtn').onclick = deletePatient;
    openModal('deleteConfirmModal');
}

async function deletePatient() {
    try {
        await del(`/patients/${patientId}`);
        showToast('Пациент удалён', 'success');
        closeModal('deleteConfirmModal');
        setTimeout(() => {
            window.location.href = 'patient-search.html';
        }, 1000);
    } catch (error) {
        showToast('Ошибка удаления: ' + error.message, 'error');
    }
}

// ==================== ДОБАВЛЕНИЕ/РЕДАКТИРОВАНИЕ СЛУЧАЯ ====================

function openAddCaseModal() {
    editingCaseId = null;
    document.getElementById('caseModalTitle').textContent = 'Добавление случая';
    document.getElementById('caseId').value = '';
    
    // Очищаем форму
    document.getElementById('caseCitizenCategory').value = '';
    document.getElementById('caseCitizenType').value = '';
    document.getElementById('caseState').value = '';
    document.getElementById('caseSocialGroup').value = '';
    document.getElementById('caseDiagnosis').value = '';
    document.getElementById('caseDiagnosisDate').value = new Date().toISOString().split('T')[0];
    document.getElementById('caseDoctor').value = '';
    document.getElementById('casePlace').value = '';
    document.getElementById('caseProfile').value = '';
    document.getElementById('caseInspection').value = '';
    document.getElementById('caseTransfer').value = '';
    document.getElementById('caseIsContact').checked = false;
    
    // Сбрасываем чекбоксы
    document.querySelectorAll('#caseLabTests input[type="checkbox"]').forEach(cb => {
        cb.checked = false;
    });
    
    // Если есть предыдущие случаи, берём данные из последнего
    if (patientData.cases && patientData.cases.length > 0) {
        const lastCase = patientData.cases[0];
        if (lastCase.citizenCategory?.id) document.getElementById('caseCitizenCategory').value = lastCase.citizenCategory.id;
        if (lastCase.citizenType?.id) document.getElementById('caseCitizenType').value = lastCase.citizenType.id;
        if (lastCase.state?.id) document.getElementById('caseState').value = lastCase.state.id;
        if (lastCase.socialGroup?.id) document.getElementById('caseSocialGroup').value = lastCase.socialGroup.id;
    }
    
    openModal('caseModal');
}

async function openEditCaseModal(caseId) {
    editingCaseId = caseId;
    document.getElementById('caseModalTitle').textContent = 'Редактирование случая';
    document.getElementById('caseId').value = caseId;
    
    try {
        // Загружаем полные данные случая
        const caseData = await get(`/detection-cases/${caseId}`);
        
        document.getElementById('caseCitizenCategory').value = caseData.citizenCategory?.id || '';
        document.getElementById('caseCitizenType').value = caseData.citizenType?.id || '';
        document.getElementById('caseState').value = caseData.state?.id || '';
        document.getElementById('caseSocialGroup').value = caseData.socialGroup?.id || '';
        document.getElementById('caseDiagnosis').value = caseData.diagnosis?.id || '';
        document.getElementById('caseDiagnosisDate').value = caseData.diagnosisDate || '';
        document.getElementById('caseDoctor').value = caseData.doctor?.id || '';
        document.getElementById('casePlace').value = caseData.place?.id || '';
        document.getElementById('caseProfile').value = caseData.profile?.id || '';
        document.getElementById('caseInspection').value = caseData.inspection?.id || '';
        document.getElementById('caseTransfer').value = caseData.transfer?.id || '';
        document.getElementById('caseIsContact').checked = caseData.isContact || false;
        
        // Устанавливаем чекбоксы лаб. тестов
        document.querySelectorAll('#caseLabTests input[type="checkbox"]').forEach(cb => {
            cb.checked = false;
        });
        
        if (caseData.labTests && caseData.labTests.length > 0) {
            caseData.labTests.forEach(test => {
                const cb = document.getElementById(`labTest_${test.id}`);
                if (cb) cb.checked = true;
            });
        }
        
        openModal('caseModal');
    } catch (error) {
        showToast('Ошибка загрузки случая: ' + error.message, 'error');
    }
}

async function saveCase() {
    // Собираем выбранные лаб. тесты
    const labTestIds = [];
    document.querySelectorAll('#caseLabTests input[type="checkbox"]:checked').forEach(cb => {
        labTestIds.push(parseInt(cb.value));
    });
    
    const data = {
        citizenCategoryId: parseInt(document.getElementById('caseCitizenCategory').value),
        citizenTypeId: parseInt(document.getElementById('caseCitizenType').value),
        stateId: parseInt(document.getElementById('caseState').value),
        socialGroupId: parseInt(document.getElementById('caseSocialGroup').value),
        diagnosisId: parseInt(document.getElementById('caseDiagnosis').value),
        diagnosisDate: document.getElementById('caseDiagnosisDate').value,
        doctorId: parseInt(document.getElementById('caseDoctor').value),
        placeId: parseInt(document.getElementById('casePlace').value),
        profileId: parseInt(document.getElementById('caseProfile').value),
        inspectionId: parseInt(document.getElementById('caseInspection').value),
        transferId: parseInt(document.getElementById('caseTransfer').value),
        isContact: document.getElementById('caseIsContact').checked,
        labTestIds: labTestIds
    };
    
    // Валидация
    const required = ['citizenCategoryId', 'citizenTypeId', 'stateId', 'socialGroupId',
                      'diagnosisId', 'diagnosisDate', 'doctorId', 'placeId',
                      'profileId', 'inspectionId', 'transferId'];
    
    for (const field of required) {
        if (!data[field]) {
            showToast('Заполните все обязательные поля', 'error');
            return;
        }
    }
    
    try {
        if (editingCaseId) {
            // Редактирование
            await put(`/detection-cases/${editingCaseId}`, data);
            showToast('Случай обновлён', 'success');
        } else {
            // Добавление
            await post(`/patients/${patientId}/cases`, data);
            showToast('Случай добавлен', 'success');
        }
        
        closeModal('caseModal');
        await loadPatient();
    } catch (error) {
        showToast('Ошибка сохранения: ' + error.message, 'error');
    }
}

// ==================== УДАЛЕНИЕ СЛУЧАЯ ====================

function confirmDeleteCase(caseId) {
    document.getElementById('deleteConfirmText').textContent = 
        'Удалить случай заболевания?';
    
    document.getElementById('deleteConfirmBtn').onclick = () => deleteCase(caseId);
    openModal('deleteConfirmModal');
}

async function deleteCase(caseId) {
    try {
        await del(`/detection-cases/${caseId}`);
        showToast('Случай удалён', 'success');
        closeModal('deleteConfirmModal');
        await loadPatient();
    } catch (error) {
        showToast('Ошибка удаления: ' + error.message, 'error');
    }
}

// ==================== УТИЛИТЫ ====================
