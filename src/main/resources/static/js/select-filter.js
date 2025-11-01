(function(){
    const registry = new WeakMap();

    function captureDomOptions(select){
        return Array.from(select.options || []).map((option, index) => ({
            value: option.value,
            text: option.textContent ?? '',
            disabled: option.disabled,
            isPlaceholder: index === 0 && option.value === ''
        }));
    }

    function deriveLabelText(select){
        const aria = select.getAttribute('aria-label');
        if (aria) return aria;
        const label = select.closest('label');
        if (!label){
            return select.getAttribute('name') || select.id || '';
        }
        const parts = [];
        label.childNodes.forEach(node => {
            if (node === select) return;
            if (node.nodeType === Node.TEXT_NODE){
                const value = node.textContent?.trim();
                if (value) parts.push(value);
                return;
            }
            if (node.nodeType === Node.ELEMENT_NODE){
                const element = /** @type {HTMLElement} */ (node);
                if (element.contains(select)) return;
                const value = element.textContent?.trim();
                if (value) parts.push(value);
            }
        });
        const text = parts.join(' ').replace(/\s+/g, ' ').trim();
        if (text) return text;
        return select.getAttribute('name') || select.id || '';
    }

    function buildOptionsFromDetail(detail){
        const options = [];
        const hasPlaceholder = detail?.hasPlaceholder !== false;
        if (hasPlaceholder){
            const placeholderText = detail?.placeholder ?? '';
            options.push({ value: '', text: String(placeholderText), disabled: false, isPlaceholder: true });
        }
        const entries = Array.isArray(detail?.options) ? detail.options : [];
        entries.forEach(entry => {
            if (!entry) return;
            const value = entry.value != null ? String(entry.value) : '';
            const label = entry.label != null ? String(entry.label) : value;
            options.push({
                value,
                text: label,
                disabled: Boolean(entry.disabled),
                isPlaceholder: false
            });
        });
        if (options.length === 0){
            options.push({ value: '', text: '', disabled: false, isPlaceholder: true });
        }
        return options;
    }

    function syncDisabled(state){
        if (!state) return;
        const select = state.select;
        const hidden = select.classList.contains('hidden') || select.hasAttribute('aria-hidden');
        if (hidden){
            state.wrapper.classList.add('hidden');
        }else{
            state.wrapper.classList.remove('hidden');
        }
        const disabled = select.disabled || hidden;
        state.input.disabled = disabled;
        state.wrapper.classList.toggle('select-filter--disabled', disabled);
    }

    function applyFilter(select){
        const state = registry.get(select);
        if (!state) return;
        const normalized = (state.filter || '').trim().toLowerCase();
        const selectedValue = select.value;
        const source = state.allOptions || [];
        const filtered = normalized ? source.filter(option => {
            if (option.isPlaceholder) return true;
            const text = (option.text || '').toLowerCase();
            const value = (option.value || '').toLowerCase();
            return text.includes(normalized) || value.includes(normalized);
        }) : source.slice();
        const fragment = document.createDocumentFragment();
        filtered.forEach(option => {
            const node = document.createElement('option');
            node.value = option.value;
            node.textContent = option.text;
            if (option.disabled && !option.isPlaceholder){
                node.disabled = true;
            }
            fragment.appendChild(node);
        });
        state.isApplying = true;
        select.innerHTML = '';
        select.appendChild(fragment);
        const hasSelection = filtered.some(option => option.value === selectedValue);
        if (hasSelection){
            select.value = selectedValue;
        }else if (state.filter){
            select.value = '';
            select.dispatchEvent(new Event('change', { bubbles: true }));
        }
        state.wrapper.dataset.hasFilter = state.filter ? 'true' : 'false';
        state.isApplying = false;
        syncDisabled(state);
    }

    function resetFilter(state){
        if (!state || !state.filter) return;
        state.filter = '';
        state.input.value = '';
        applyFilter(state.select);
    }

    function attach(select){
        if (!select || registry.has(select)) return;
        const state = {
            select,
            filter: '',
            allOptions: captureDomOptions(select),
            input: null,
            wrapper: null,
            observer: null,
            isApplying: false
        };
        const labelText = deriveLabelText(select);
        const wrapper = document.createElement('div');
        wrapper.className = 'select-filter';
        const input = document.createElement('input');
        input.type = 'search';
        input.className = 'select-filter__input';
        input.placeholder = 'Tippen, um zu filtern…';
        input.autocomplete = 'off';
        input.spellcheck = false;
        input.setAttribute('role', 'searchbox');
        input.setAttribute('aria-label', labelText ? `Optionen für ${labelText} filtern` : 'Optionen filtern');

        const parent = select.parentNode;
        if (!parent) return;
        parent.insertBefore(wrapper, select);
        wrapper.appendChild(input);
        wrapper.appendChild(select);

        state.input = input;
        state.wrapper = wrapper;
        registry.set(select, state);

        input.addEventListener('focus', () => {
            wrapper.classList.add('select-filter--active');
        });
        select.addEventListener('focus', () => {
            wrapper.classList.add('select-filter--active');
        });

        const scheduleReset = () => {
            requestAnimationFrame(() => {
                const active = document.activeElement;
                if (!active || !wrapper.contains(active)){
                    wrapper.classList.remove('select-filter--active');
                    resetFilter(state);
                }
            });
        };
        input.addEventListener('blur', scheduleReset);
        select.addEventListener('blur', scheduleReset);

        input.addEventListener('input', () => {
            state.filter = input.value;
            applyFilter(select);
        });
        input.addEventListener('keydown', event => {
            if (event.key === 'ArrowDown'){
                event.preventDefault();
                select.focus();
            }else if (event.key === 'Escape'){
                if (state.filter){
                    event.preventDefault();
                    resetFilter(state);
                }
            }
        });

        select.addEventListener('keydown', event => {
            if (event.key === 'Escape' && state.filter){
                event.preventDefault();
                resetFilter(state);
                input.focus();
                return;
            }
            if (event.key === 'Backspace' && input.value){
                event.preventDefault();
                input.focus();
                input.value = input.value.slice(0, -1);
                input.dispatchEvent(new Event('input'));
                return;
            }
            if (event.key.length === 1 && !event.ctrlKey && !event.metaKey && !event.altKey){
                event.preventDefault();
                input.focus();
                const current = input.value || '';
                input.value = current + event.key;
                input.dispatchEvent(new Event('input'));
            }
        });

        select.addEventListener('options-populated', event => {
            if (state.isApplying) return;
            state.allOptions = buildOptionsFromDetail(event.detail);
            applyFilter(select);
        });

        const observer = new MutationObserver(mutations => {
            if (state.isApplying) return;
            for (const mutation of mutations){
                if (mutation.type === 'attributes'){
                    syncDisabled(state);
                }
            }
        });
        observer.observe(select, { attributes: true, attributeFilter: ['disabled', 'class', 'aria-hidden'] });
        state.observer = observer;
        syncDisabled(state);
        applyFilter(select);
    }

    function enhance(root){
        if (!root) return;
        const elements = root instanceof HTMLSelectElement ? [root] : Array.from(root.querySelectorAll('select'));
        elements.forEach(select => attach(select));
    }

    window.enhanceSelectFiltering = enhance;
    document.addEventListener('DOMContentLoaded', () => enhance(document));
})();
