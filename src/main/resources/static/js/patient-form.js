/**
 * KVD Application - Patient Form (Data Entry)
 */

document.addEventListener('DOMContentLoaded', () => {
    initPatientForm();
});

async function initPatientForm() {
    // Устанавливаем текущую дату по умолчанию
    const today = new Date().toISOString().split('T')[0];
    document.getElementById('diagnosisDate').value = today;
    
    // Загружаем все справочники
    await loadAllDictionaries();
    
    // Загружаем лабораторные тесты
    await loadLabTests();
    
    // Подсказки адреса через DaData
    initAddressAutocomplete('address');
}

async function loadAllDictionaries() {
    try {
        await Promise.all([
            loadSelectOptions('gender', '/dictionaries/genders'),
            loadSelectOptions('citizenCategory', '/dictionaries/citizen-categories'),
            loadSelectOptions('citizenType', '/dictionaries/citizen-types'),
            loadSelectOptions('state', '/dictionaries/states'),
            loadSelectOptions('socialGroup', '/dictionaries/social-groups'),
            loadSelectOptions('diagnosis', '/dictionaries/diagnoses'),
            loadSelectOptions('place', '/dictionaries/places'),
            loadSelectOptions('profile', '/dictionaries/profiles'),
            loadSelectOptions('inspection', '/dictionaries/inspections'),
            loadSelectOptions('transfer', '/dictionaries/transfers'),
        ]);
        
        // Врачи загружаются отдельно (особый формат ФИО)
        await loadDoctorsSelect('doctor');
    } catch (error) {
        showToast('Ошибка загрузки справочников', 'error');
        console.error(error);
    }
}

// Загрузка врачей с форматированием ФИО
async function loadDoctorsSelect(selectId) {
    const select = document.getElementById(selectId);
    if (!select) return;
    
    try {
        const doctors = await get('/dictionaries/doctors');
        select.innerHTML = '<option value="">Выберите...</option>';
        
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

async function loadLabTests() {
    const container = document.getElementById('labTestsContainer');
    if (!container) return;
    
    try {
        const labTests = await get('/dictionaries/lab-test-types');
        
        container.innerHTML = labTests.map(test => `
            <label class="checkbox-item">
                <input type="checkbox" name="labTests" value="${test.id}">
                <span>${test.name}</span>
            </label>
        `).join('');
    } catch (error) {
        console.error('Error loading lab tests:', error);
        container.innerHTML = '<p class="error">Ошибка загрузки</p>';
    }
}

async function savePatientForm() {
    try {
        // Собираем данные формы
        const data = {
            // Данные пациента
            lastName: document.getElementById('lastName').value.trim(),
            firstName: document.getElementById('firstName').value.trim(),
            middleName: document.getElementById('middleName').value.trim() || null,
            birthDate: document.getElementById('birthDate').value,
            genderId: parseInt(document.getElementById('gender').value),
            address: document.getElementById('address').value.trim() || null,
            
            // Социальные данные
            citizenCategoryId: parseInt(document.getElementById('citizenCategory').value),
            citizenTypeId: parseInt(document.getElementById('citizenType').value),
            stateId: parseInt(document.getElementById('state').value),
            socialGroupId: parseInt(document.getElementById('socialGroup').value),
            
            // Данные о заболевании
            diagnosisId: parseInt(document.getElementById('diagnosis').value),
            diagnosisDate: document.getElementById('diagnosisDate').value,
            doctorId: parseInt(document.getElementById('doctor').value),
            placeId: parseInt(document.getElementById('place').value),
            profileId: parseInt(document.getElementById('profile').value),
            inspectionId: parseInt(document.getElementById('inspection').value),
            transferId: parseInt(document.getElementById('transfer').value),
            isContact: document.getElementById('isContact')?.checked || false,
            
            // Лабораторные тесты
            labTestIds: getSelectedLabTests(),
        };
        
        // Валидация обязательных полей
        validatePatientForm(data);
        
        // Отправляем на сервер
        const result = await post('/detection-cases', data);
        
        showToast('Случай успешно сохранён', 'success');
        
        // Очищаем форму для следующего ввода
        clearPatientForm();
        
    } catch (error) {
        showToast(error.message, 'error');
        console.error(error);
    }
}

function validatePatientForm(data) {
    const requiredFields = {
        lastName: 'Фамилия',
        firstName: 'Имя',
        birthDate: 'Дата рождения',
        genderId: 'Пол',
        citizenCategoryId: 'Категория проживания',
        citizenTypeId: 'Тип населённого пункта',
        stateId: 'Район',
        socialGroupId: 'Социальная группа',
        diagnosisId: 'Диагноз',
        diagnosisDate: 'Дата диагноза',
        doctorId: 'Врач',
        placeId: 'Место выявления',
        profileId: 'Профиль',
        inspectionId: 'Осмотр',
        transferId: 'Путь передачи',
    };
    
    for (const [field, label] of Object.entries(requiredFields)) {
        const value = data[field];
        
        // Проверяем на null/undefined/пустую строку
        if (value == null || value === '') {
            throw new Error(`Заполните поле "${label}"`);
        }
        
        // Проверяем числа на NaN
        if (typeof value === 'number' && Number.isNaN(value)) {
            throw new Error(`Заполните поле "${label}"`);
        }
    }
}

function getSelectedLabTests() {
    const checkboxes = document.querySelectorAll('input[name="labTests"]:checked');
    return Array.from(checkboxes).map(cb => parseInt(cb.value));
}

function clearPatientForm() {
    // Очищаем текстовые поля
    document.getElementById('lastName').value = '';
    document.getElementById('firstName').value = '';
    document.getElementById('middleName').value = '';
    document.getElementById('birthDate').value = '';
    document.getElementById('address').value = '';
    
    // Сбрасываем селекты
    document.getElementById('gender').value = '';
    document.getElementById('citizenCategory').value = '';
    document.getElementById('citizenType').value = '';
    document.getElementById('state').value = '';
    document.getElementById('socialGroup').value = '';
    document.getElementById('diagnosis').value = '';
    document.getElementById('doctor').value = '';
    document.getElementById('place').value = '';
    document.getElementById('profile').value = '';
    document.getElementById('inspection').value = '';
    document.getElementById('transfer').value = '';
    
    // Устанавливаем текущую дату
    document.getElementById('diagnosisDate').value = new Date().toISOString().split('T')[0];
    
    // Сбрасываем чекбоксы
    document.querySelectorAll('input[name="labTests"]').forEach(cb => {
        cb.checked = false;
    });
    
    if (document.getElementById('isContact')) {
        document.getElementById('isContact').checked = false;
    }
    
    // Фокус на первое поле
    document.getElementById('lastName').focus();
}

function cancelPatientForm() {
    if (confirmAction('Вы уверены? Несохранённые данные будут потеряны.')) {
        clearPatientForm();
    }
}
