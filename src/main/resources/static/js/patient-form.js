/**
 * KVD Application - Patient Form (Data Entry)
 */

// Карта диагнозов: { id -> { name, groupName } }
// Заполняется при загрузке страницы и используется для проверок при сохранении.
let diagnosesMap = {};

// Группы диагнозов, относящиеся к ИППП.
// Предупреждение показывается если пациенту меньше 18 лет и диагноз из этих групп.
const IPPI_GROUPS = ['Сифилис', 'Гонорея', 'Хламидиоз', 'Трихомониаз', 'Герпес', 'Остроконечные кондиломы'];

// ==================== МОДАЛЬНОЕ ОКНО ПРЕДУПРЕЖДЕНИЯ ====================

// Хранит функцию resolve() текущего Promise.
let warningModalResolveFn = null;

// Показывает модальное окно предупреждения с двумя кнопками.
// Возвращает Promise: true если нажали "Продолжить", false если "Исправить" или закрыли.
function showWarningModal(message) {
    return new Promise((resolve) => {
        warningModalResolveFn = resolve;
        document.getElementById('warningModalText').textContent = message;
        openModal('warningModal');
    });
}

// Вызывается при нажатии кнопок модалки или крестика.
// choice: true = продолжить, false = исправить/отмена
function warningModalResolve(choice) {
    closeModal('warningModal');
    if (warningModalResolveFn !== null) {
        warningModalResolveFn(choice);
        warningModalResolveFn = null;
    }
}

// =====================================================================

document.addEventListener('DOMContentLoaded', () => {
    initPatientForm();
});

async function initPatientForm() {
    // Устанавливаем текущую дату по умолчанию
    const today = new Date().toISOString().split('T')[0];
    document.getElementById('diagnosisDate').value = today;

    await loadAllDictionaries();
    await loadLabTests();
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
            // Диагнозы загружаем отдельно — нужно сохранить группу каждого диагноза
            loadDiagnosesWithGroups(),
            loadSelectOptions('place', '/dictionaries/places'),
            loadSelectOptions('profile', '/dictionaries/profiles'),
            loadSelectOptions('inspection', '/dictionaries/inspections'),
            loadSelectOptions('transfer', '/dictionaries/transfers'),
        ]);

        await loadDoctorsSelect('doctor');
    } catch (error) {
        showToast('Ошибка загрузки справочников', 'error');
        console.error(error);
    }
}

// Загружает диагнозы и заполняет diagnosesMap.
// Сервер возвращает каждый диагноз вместе с вложенным объектом diagnosisGroup.
async function loadDiagnosesWithGroups() {
    const select = document.getElementById('diagnosis');
    if (!select) return;

    try {
        const diagnoses = await get('/dictionaries/diagnoses');
        select.innerHTML = '<option value="">Выберите...</option>';

        diagnoses.forEach(diagnosis => {
            const option = document.createElement('option');
            option.value = diagnosis.id;
            option.textContent = diagnosis.name;
            select.appendChild(option);

            // Сохраняем в карту для проверки ИППП при сохранении
            diagnosesMap[diagnosis.id] = {
                name: diagnosis.name,
                groupName: diagnosis.diagnosisGroup?.name || ''
            };
        });
    } catch (error) {
        console.error('Error loading diagnoses:', error);
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
            if (doctor.firstName) fullName += ` ${doctor.firstName.charAt(0)}.`;
            if (doctor.middleName) fullName += `${doctor.middleName.charAt(0)}.`;

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
        const data = {
            lastName: document.getElementById('lastName').value.trim(),
            firstName: document.getElementById('firstName').value.trim(),
            middleName: document.getElementById('middleName').value.trim() || null,
            birthDate: document.getElementById('birthDate').value,
            genderId: parseInt(document.getElementById('gender').value),
            address: document.getElementById('address').value.trim() || null,

            citizenCategoryId: parseInt(document.getElementById('citizenCategory').value),
            citizenTypeId: parseInt(document.getElementById('citizenType').value),
            stateId: parseInt(document.getElementById('state').value),
            socialGroupId: parseInt(document.getElementById('socialGroup').value),

            diagnosisId: parseInt(document.getElementById('diagnosis').value),
            diagnosisDate: document.getElementById('diagnosisDate').value,
            doctorId: parseInt(document.getElementById('doctor').value),
            placeId: parseInt(document.getElementById('place').value),
            profileId: parseInt(document.getElementById('profile').value),
            inspectionId: parseInt(document.getElementById('inspection').value),
            transferId: parseInt(document.getElementById('transfer').value),
            isContact: document.getElementById('isContact')?.checked || false,

            labTestIds: getSelectedLabTests(),
        };

        // ── Предупреждение 1: год рождения совпадает с текущим годом ──────
        if (data.birthDate) {
            const birthYear = new Date(data.birthDate).getFullYear();
            const currentYear = new Date().getFullYear();

            if (birthYear === currentYear) {
                const confirmed = await showWarningModal(
                    `Год рождения пациента — ${currentYear} (текущий год).\n\n` +
                    `Возможно, вместо даты рождения указана дата постановки диагноза. ` +
                    `Проверьте дату рождения перед сохранением.`
                );
                if (!confirmed) return;
            }
        }

        // ── Предупреждение 2: несовершеннолетний пациент + ИППП ───────────
        if (data.birthDate) {
            const birth = new Date(data.birthDate);
            const today = new Date();
            let age = today.getFullYear() - birth.getFullYear();
            const monthDiff = today.getMonth() - birth.getMonth();
            if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) {
                age--;
            }

            const selectedGroupName = diagnosesMap[data.diagnosisId]?.groupName || '';
            const isIppiDiagnosis = IPPI_GROUPS.includes(selectedGroupName);

            if (age < 18 && isIppiDiagnosis) {
                const confirmed = await showWarningModal(
                    `Возраст пациента — ${age} лет (менее 18).\n\n` +
                    `Выбранный диагноз относится к группе "${selectedGroupName}" (ИППП). ` +
                    `Убедитесь в правильности данных перед сохранением.`
                );
                if (!confirmed) return;
            }
        }
        // ──────────────────────────────────────────────────────────────────

        // Валидация обязательных полей
        validatePatientForm(data);

        // Отправляем на сервер
        await post('/detection-cases', data);

        showToast('Случай успешно сохранён', 'success');
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
        if (value == null || value === '') {
            throw new Error(`Заполните поле "${label}"`);
        }
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
    document.getElementById('lastName').value = '';
    document.getElementById('firstName').value = '';
    document.getElementById('middleName').value = '';
    document.getElementById('birthDate').value = '';
    document.getElementById('address').value = '';

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

    document.getElementById('diagnosisDate').value = new Date().toISOString().split('T')[0];

    document.querySelectorAll('input[name="labTests"]').forEach(cb => {
        cb.checked = false;
    });

    if (document.getElementById('isContact')) {
        document.getElementById('isContact').checked = false;
    }

    document.getElementById('lastName').focus();
}

function cancelPatientForm() {
    if (confirmAction('Вы уверены? Несохранённые данные будут потеряны.')) {
        clearPatientForm();
    }
}
