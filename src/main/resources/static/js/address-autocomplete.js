/**
 * KVD Application - Address Autocomplete (DaData)
 *
 * Подключается на страницах с полем адреса: patient-form.html, patient-card.html
 *
 * Как работает:
 * 1) Пользователь печатает в поле адреса
 * 2) После 300мс паузы → запрос на наш сервер → DaData
 * 3) Под полем появляется выпадающий список с вариантами
 * 4) Можно выбрать вариант ИЛИ продолжить печатать вручную
 * 5) Если DaData недоступна — поле работает как обычный input
 */

/**
 * Инициализирует автокомплит на текстовом поле.
 * @param {string} inputId — ID поля ввода ('address' или 'editAddress')
 */
function initAddressAutocomplete(inputId) {
    const input = document.getElementById(inputId);
    if (!input) return;

    // Если уже инициализирован — не дублируем
    if (input.dataset.autocompleteInit === 'true') return;
    input.dataset.autocompleteInit = 'true';

    // Оборачиваем input в div с position:relative (для позиционирования dropdown)
    let wrapper = input.parentElement;
    if (!wrapper.classList.contains('autocomplete-wrapper')) {
        const newWrapper = document.createElement('div');
        newWrapper.className = 'autocomplete-wrapper';
        wrapper.insertBefore(newWrapper, input);
        newWrapper.appendChild(input);
        wrapper = newWrapper;
    }

    // Создаём выпадающий список
    const dropdown = document.createElement('div');
    dropdown.className = 'autocomplete-dropdown';
    dropdown.style.display = 'none';
    wrapper.appendChild(dropdown);

    let debounceTimer = null;
    let activeIndex = -1;

    // ── Ввод текста ──
    input.addEventListener('input', () => {
        const query = input.value.trim();
        clearTimeout(debounceTimer);
        activeIndex = -1;

        // Минимум 3 символа для поиска
        if (query.length < 3) {
            hideDropdown(dropdown);
            return;
        }

        // Debounce 300мс — экономим запросы
        debounceTimer = setTimeout(() => {
            fetchSuggestions(query, dropdown, input);
        }, 300);
    });

    // ── Навигация клавиатурой ──
    input.addEventListener('keydown', (e) => {
        const items = dropdown.querySelectorAll('.autocomplete-item');
        if (items.length === 0) return;

        if (e.key === 'ArrowDown') {
            e.preventDefault();
            activeIndex = Math.min(activeIndex + 1, items.length - 1);
            updateActiveItem(items, activeIndex);
        } else if (e.key === 'ArrowUp') {
            e.preventDefault();
            activeIndex = Math.max(activeIndex - 1, 0);
            updateActiveItem(items, activeIndex);
        } else if (e.key === 'Enter') {
            if (activeIndex >= 0 && items[activeIndex]) {
                e.preventDefault();
                input.value = items[activeIndex].textContent;
                hideDropdown(dropdown);
            }
        } else if (e.key === 'Escape') {
            hideDropdown(dropdown);
        }
    });

    // ── Закрытие при клике вне поля ──
    document.addEventListener('click', (e) => {
        if (!wrapper.contains(e.target)) {
            hideDropdown(dropdown);
        }
    });
}

/**
 * Запрос подсказок через наш сервер (прокси к DaData).
 */
async function fetchSuggestions(query, dropdown, input) {
    try {
        const response = await post('/address/suggest', {
            query: query,
            count: 5
        });

        const suggestions = response.suggestions || [];

        if (suggestions.length === 0) {
            hideDropdown(dropdown);
            return;
        }

        showSuggestions(suggestions, dropdown, input);

    } catch (error) {
        // DaData недоступна — просто не показываем подсказки
        console.warn('Подсказки адреса недоступны:', error.message);
        hideDropdown(dropdown);
    }
}

/**
 * Отображает список подсказок.
 */
function showSuggestions(suggestions, dropdown, input) {
    dropdown.innerHTML = '';

    suggestions.forEach((item) => {
        const div = document.createElement('div');
        div.className = 'autocomplete-item';
        div.textContent = item.value;

        // mousedown (не click) — чтобы сработало до blur
        div.addEventListener('mousedown', (e) => {
            e.preventDefault();
            input.value = item.value;
            hideDropdown(dropdown);
        });

        dropdown.appendChild(div);
    });

    dropdown.style.display = 'block';
}

function hideDropdown(dropdown) {
    dropdown.style.display = 'none';
    dropdown.innerHTML = '';
}

function updateActiveItem(items, activeIndex) {
    items.forEach(item => item.classList.remove('active'));
    if (items[activeIndex]) {
        items[activeIndex].classList.add('active');
        items[activeIndex].scrollIntoView({ block: 'nearest' });
    }
}
