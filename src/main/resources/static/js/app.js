/* ===================== Konfiguration ===================== */
const API = {
    progress: '/api/index-progress',   // liefert IndexProgress.Status
    reindex:  '/api/index/reindex',    // POST → manueller Reindex
    search:   '/search',               // GET /search?q=...
    suggest:  '/search/suggest',       // GET /search/suggest?q=...
    table:    '/table',                // GET /table/{name}
};

/* ===================== DOM-Referenzen ===================== */
const resultArea  = document.getElementById('resultArea');
const searchInput = document.getElementById('search-input');
const searchBtn   = document.getElementById('search-btn');

const TABLE_QUERY_FRAGMENTS = {
    account: {
        status: [
            { label: 'Aktive Accounts', query: 'statusactive' },
            { label: 'Inaktive Accounts', query: 'statusinactive' },
        ],
        country: [
            { label: '🇦🇹 Österreich', query: 'country:"Austria"' },
            { label: '🇩🇪 Deutschland', query: 'country:"Germany"' },
        ],
        contactemail: [
            { label: 'Mit Kontakt-E-Mail', query: 'contactemail:*' },
        ],
    },
    project: {
        stillactive: [
            { label: 'Aktive Projekte', query: 'statusactive' },
            { label: 'Abgeschlossene Projekte', query: 'statusinactive' },
        ],
        bundletype: [
            { label: 'Bundle vorhanden', query: 'bundle:*' },
            { label: 'Ohne Bundle', query: '-bundle:*' },
        ],
        deploymentvariantid: [
            { label: 'Mit Deployment-Variante', query: 'deploymentvariantid:*' },
        ],
    },
    site: {
        firezone: [
            { label: 'Zone Alpha', query: 'zonealpha' },
            { label: 'Zone Bravo', query: 'zonebravo' },
            { label: 'Zone Charlie', query: 'zonecharlie' },
        ],
        tenantcount: [
            { label: '≥ 50 Einheiten', query: 'tenantcount:[50 TO *]' },
            { label: '≤ 10 Einheiten', query: 'tenantcount:[0 TO 10]' },
        ],
    },
    server: {
        serverbrand: [
            { label: 'Lenovo', query: 'serverbrand:lenovo' },
            { label: 'Dell', query: 'serverbrand:dell' },
        ],
        serveros: [
            { label: 'Windows Server', query: 'serveros:windows*' },
            { label: 'Linux Server', query: 'serveros:linux*' },
        ],
        virtualplatform: [
            { label: 'Virtuelle Maschinen', query: 'virtualplatform:*' },
        ],
    },
    clients: {
        clientos: [
            { label: 'Windows', query: 'clientos:windows*' },
            { label: 'macOS', query: 'clientos:mac*' },
        ],
        clientbrand: [
            { label: 'Lenovo', query: 'clientbrand:lenovo' },
            { label: 'HP', query: 'clientbrand:hp' },
        ],
    },
    radio: {
        mode: [
            { label: 'Analog', query: 'mode:analog' },
            { label: 'Digital', query: 'mode:digital' },
        ],
        digitalstandard: [
            { label: 'TETRA', query: 'digitalstandard:tetra' },
            { label: 'DMR', query: 'digitalstandard:dmr' },
        ],
    },
    audiodevice: {
        direction: [
            { label: 'Input', query: 'direction:input' },
            { label: 'Output', query: 'direction:output' },
        ],
        audiodevicebrand: [
            { label: 'Sennheiser', query: 'audiodevicebrand:sennheiser' },
            { label: 'Poly', query: 'audiodevicebrand:poly' },
        ],
    },
    phoneintegration: {
        phonetype: [
            { label: 'Desk Phones', query: 'phonetype:desk' },
            { label: 'Softphones', query: 'phonetype:soft' },
        ],
        phonebrand: [
            { label: 'Cisco', query: 'phonebrand:cisco' },
            { label: 'Microsoft Teams', query: 'phonebrand:"Microsoft"' },
        ],
    },
};

const idxBox  = document.getElementById('idx-box');
const idxBar  = document.querySelector('#idx-bar > span');
const idxText = document.getElementById('idx-text');
const idxBtnSide = document.getElementById('idx-reindex-side');

const sugList = document.getElementById('sug');

/* ===================== Utils ===================== */
const sleep = (ms) => new Promise(r => setTimeout(r, ms));
const debounce = (fn, ms=250) => { let t; return (...a)=>{clearTimeout(t); t=setTimeout(()=>fn(...a),ms);} };
const stGet = (k, d) => { try { const v = localStorage.getItem(k); return v === null ? d : v; } catch { return d; } };
const stSet = (k, v) => { try { localStorage.setItem(k, v); } catch {} };
function escapeHtml(s){ return (s??'').replace(/[&<>"']/g, c=>({ '&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;' }[c])); }
function setBusy(el, busy){ if(!el) return; busy ? el.setAttribute('aria-busy','true') : el.removeAttribute('aria-busy'); }

const normalizeKey = (value) => (value ?? '').toString().trim().toLowerCase().replace(/[^a-z0-9]+/g, '');

function getColumnFragments(tableName, columnName){
    if (!tableName || !columnName) return [];
    const tableKey = normalizeKey(tableName);
    const columnKey = normalizeKey(columnName);
    const tableConfig = TABLE_QUERY_FRAGMENTS[tableKey];
    if (!tableConfig) return [];
    const options = tableConfig[columnKey];
    if (!Array.isArray(options)) return [];
    return options.filter(opt => opt && typeof opt.query === 'string' && opt.query.trim());
}

function splitQueryFragments(query){
    const result = [];
    if (!query) return result;
    let current = '';
    let inQuotes = false;
    for (let i = 0; i < query.length; i += 1){
        const ch = query[i];
        if (ch === '"'){
            inQuotes = !inQuotes;
            current += ch;
            continue;
        }
        if (!inQuotes){
            const slice = query.slice(i, i + 5);
            if (slice.toUpperCase() === ' AND '){
                const trimmed = current.trim();
                if (trimmed) result.push(trimmed);
                current = '';
                i += 4;
                continue;
            }
        }
        current += ch;
    }
    const trimmed = current.trim();
    if (trimmed) result.push(trimmed);
    return result;
}

const normalizeFragment = (fragment) => fragment.replace(/\s+/g, ' ').trim().toLowerCase();

function formatFragment(fragment){
    let trimmed = (fragment || '').trim();
    if (!trimmed) return '';
    const hasQuotes = /^".*"$/.test(trimmed);
    if (!hasQuotes && /\s/.test(trimmed) && !/[():]/.test(trimmed)){
        trimmed = `"${trimmed}"`;
    }
    return trimmed;
}

function mergeQueryWithFragment(currentQuery, fragment){
    const formatted = formatFragment(fragment);
    if (!formatted) return currentQuery || '';
    const fragments = splitQueryFragments(currentQuery || '');
    const normalized = new Set(fragments.map(normalizeFragment));
    const key = normalizeFragment(formatted);
    if (!normalized.has(key)){
        fragments.push(formatted);
    }
    return fragments.join(' AND ');
}

function queryContainsFragment(fragment, query){
    if (!fragment) return false;
    const formatted = formatFragment(fragment);
    if (!formatted) return false;
    const fragments = splitQueryFragments(query || (searchInput ? searchInput.value : ''));
    const key = normalizeFragment(formatted);
    return fragments.some(f => normalizeFragment(f) === key);
}

function applyQueryFragment(fragment){
    if (!searchInput) return;
    const merged = mergeQueryWithFragment(searchInput.value, fragment);
    searchInput.value = merged;
    refreshActiveTableMarkers();
    runSearch(searchInput.value);
}

function refreshActiveTableMarkers(){
    if (!resultArea) return;
    const buttons = resultArea.querySelectorAll('.table-title-button');
    buttons.forEach(btn => {
        const frag = btn.dataset.fragment;
        const isActive = queryContainsFragment(frag);
        btn.classList.toggle('is-active', isActive);
        if (isActive){
            btn.setAttribute('aria-pressed', 'true');
        } else {
            btn.removeAttribute('aria-pressed');
        }
    });
}

if (resultArea){
    resultArea.addEventListener('click', (event) => {
        const trigger = event.target.closest('.table-fragment-option, .table-title-button');
        if (!trigger) return;
        const fragment = trigger.dataset.fragment;
        if (!fragment) return;
        event.preventDefault();
        applyQueryFragment(fragment);
    });
}

const shortcutCache = new Map();

function parseBool(value){
    if (typeof value === 'boolean') return value;
    const normalized = String(value ?? '').trim().toLowerCase();
    if (!normalized) return false;
    return ['true','1','yes','y','ja','wahr'].includes(normalized);
}

function formatDateLabel(value){
    if (value === null || value === undefined) return '';
    const str = String(value).trim();
    if (!str) return '';
    const timestamp = Date.parse(str);
    if (!Number.isNaN(timestamp)){
        return new Date(timestamp).toLocaleDateString('de-AT', { year: 'numeric', month: '2-digit', day: '2-digit' });
    }
    return str.length > 16 ? str.slice(0, 16) : str;
}

function formatDateRange(start, end){
    const from = formatDateLabel(start);
    const to   = formatDateLabel(end);
    if (from && to) return `${from} → ${to}`;
    return from || to || '';
}

async function getShortcutItems(kind){
    if (!kind) return [];
    const key = kind.toString();
    if (!shortcutCache.has(key)){
        shortcutCache.set(key, loadShortcutItems(key).catch(err => { shortcutCache.delete(key); throw err; }));
    }
    return shortcutCache.get(key);
}

async function loadShortcutItems(kind){
    const key = (kind || '').toString().toLowerCase();
    switch (key){
        case 'projects-active': {
            const res = await fetch('/projects');
            if (!res.ok) throw new Error(`HTTP ${res.status}`);
            const rows = await res.json();
            return rows
                .filter(row => parseBool(val(row,'StillActive','stillActive','still_active')))
                .map(row => {
                    const id       = val(row,'ProjectID');
                    const name     = val(row,'ProjectName');
                    const sap      = val(row,'ProjectSAPID');
                    const bundle   = val(row,'BundleType');
                    const variant  = val(row,'DeploymentVariantID');
                    const meta = [];
                    if (sap) meta.push(`SAP ${sap}`);
                    if (bundle) meta.push(bundle);
                    if (variant) meta.push(`Var. ${shortUuid(variant)}`);
                    const primary = (name && name.trim()) || (sap ? `Projekt ${sap}` : (id ? `Projekt ${shortUuid(id)}` : 'Projekt'));
                    return {
                        primary,
                        secondary: meta.join(' · ') || null,
                        action: id ? { type: 'details', entity: 'project', id } : null,
                    };
                })
                .sort((a, b) => (a.primary || '').localeCompare(b.primary || '', 'de', { sensitivity: 'base' }));
        }
        case 'servicecontracts-progress': {
            const res = await fetch(`${API.table}/servicecontract?limit=200`);
            if (!res.ok) throw new Error(`HTTP ${res.status}`);
            const rows = await res.json();
            return rows
                .filter(row => {
                    const status = (val(row,'Status') ?? '').toString().trim().toLowerCase();
                    return status === 'inprogress';
                })
                .map(row => {
                    const id        = val(row,'ContractID');
                    const number    = val(row,'ContractNumber');
                    const projectId = val(row,'ProjectID');
                    const siteId    = val(row,'SiteID');
                    const range     = formatDateRange(val(row,'StartDate'), val(row,'EndDate'));
                    const meta = [];
                    if (range) meta.push(range);
                    if (projectId) meta.push(`Projekt ${shortUuid(projectId)}`);
                    if (siteId) meta.push(`Site ${shortUuid(siteId)}`);
                    const primary = number ? `Vertrag ${number}` : (id ? `Vertrag ${shortUuid(id)}` : 'Vertrag');
                    const query   = id ? `id:"${id}"` : null;
                    return {
                        primary,
                        secondary: meta.join(' · ') || null,
                        action: query ? { type: 'search', query } : null,
                    };
                })
                .sort((a, b) => (a.primary || '').localeCompare(b.primary || '', 'de', { sensitivity: 'base' }));
        }
        case 'sites-bravo': {
            const res = await fetch('/sites');
            if (!res.ok) throw new Error(`HTTP ${res.status}`);
            const rows = await res.json();
            return rows
                .filter(row => {
                    const zone = (val(row,'FireZone') ?? '').toString().trim().toLowerCase();
                    return zone === 'bravo';
                })
                .map(row => {
                    const id        = val(row,'SiteID');
                    const name      = val(row,'SiteName');
                    const tenants   = val(row,'TenantCount');
                    const projectId = val(row,'ProjectID');
                    const zone      = val(row,'FireZone');
                    const meta = [];
                    if (zone) meta.push(`Zone ${zone}`);
                    if (tenants !== undefined && tenants !== null && tenants !== '') meta.push(`${tenants} Einheiten`);
                    if (projectId) meta.push(`Projekt ${shortUuid(projectId)}`);
                    const primary = (name && name.trim()) || (id ? `Site ${shortUuid(id)}` : 'Site');
                    return {
                        primary,
                        secondary: meta.join(' · ') || null,
                        action: id ? { type: 'details', entity: 'site', id } : null,
                    };
                })
                .sort((a, b) => (a.primary || '').localeCompare(b.primary || '', 'de', { sensitivity: 'base' }));
        }
        default:
            return [];
    }
}

function renderShortcutItems(listEl, items){
    if (!listEl) return;
    if (!Array.isArray(items) || !items.length){
        listEl.innerHTML = '<p class="sc-status empty">(keine Einträge)</p>';
        return;
    }
    const summary = `<div class="sc-summary">${items.length} Einträge</div>`;
    const list = items.map((item, idx) => `
        <li role="listitem">
            <button type="button" class="sc-item${item.action ? '' : ' is-static'}" data-idx="${idx}">
                <span class="sc-item-primary">${escapeHtml(item.primary || '')}</span>
                ${item.secondary ? `<span class="sc-item-secondary">${escapeHtml(item.secondary)}</span>` : ''}
            </button>
        </li>`).join('');
    listEl.innerHTML = `${summary}<ul class="sc-list">${list}</ul>`;
    const buttons = listEl.querySelectorAll('.sc-item');
    buttons.forEach(btn => {
        const idx = Number(btn.dataset.idx);
        const item = items[idx];
        if (!item || !item.action){
            btn.disabled = true;
            btn.classList.add('is-static');
            return;
        }
        const action = item.action;
        if (action.type === 'details' && action.entity && action.id){
            btn.addEventListener('click', () => toDetails(action.entity, action.id));
        } else if (action.type === 'search' && action.query){
            btn.addEventListener('click', () => runSearch(action.query));
        } else {
            btn.disabled = true;
            btn.classList.add('is-static');
        }
    });
}

async function renderShortcutList(sc, listEl){
    if (!sc || !listEl) return;
    const kind = sc.dataset.list;
    if (!kind){
        listEl.innerHTML = '<p class="sc-status empty">(keine Datenquelle)</p>';
        return;
    }
    listEl.innerHTML = '<p class="sc-status">Lade …</p>';
    try {
        const items = await getShortcutItems(kind);
        renderShortcutItems(listEl, items);
    } catch (err){
        console.error('Shortcut-Liste konnte nicht geladen werden', err);
        listEl.innerHTML = '<p class="sc-status error">Fehler beim Laden der Daten.</p>';
    }
}

/* ---------- Lucene-Heuristik & QoL-Query-Builder ---------- */
function looksLikeLucene(q){
    if (!q) return false;
    const s = q.trim();
    return s.includes(':') || s.includes('"') || s.includes(' AND ') || s.includes(' OR ') || s.endsWith('*');
}
/** Nutzerfreundlich: bei „normalem Text“ automatisch Prefix-Suche (token*) */
function buildUserQuery(raw){
    const s = (raw || '').trim();
    if (!s) return s;
    if (looksLikeLucene(s)) return s;
    return s.split(/\s+/).map(tok => /[*?]$/.test(tok) ? tok : (tok + '*')).join(' ');
}

/* ===================== Ergebnis-Vorschau (Anreicherung) ===================== */
function tableForType(t){
    const s=(t||'').toLowerCase();
    switch(s){
        case 'account': return 'Account';
        case 'project': return 'Project';
        case 'site':    return 'Site';
        case 'server':  return 'Server';
        case 'client':  return 'WorkingPosition'; // Sonderfall
        case 'radio':   return 'Radio';
        case 'audio':   return 'AudioDevice';
        case 'phone':   return 'PhoneIntegration';
        default:        return t;
    }
}
function val(row, ...keys){
    for(const k of keys){
        if (row[k] !== undefined) return row[k];
        const u = k.toUpperCase(), l = k.toLowerCase();
        for (const kk in row) { if (kk === u || kk === l) return row[kk]; }
    }
    return undefined;
}
function formatPreview(type, row){
    const t=(type||'').toLowerCase();
    const parts=[];
    if (t==='account'){
        const contact = val(row,'ContactName') || val(row,'AccountName');
        if (contact) parts.push(contact);
        const country = val(row,'Country'); if (country) parts.push(country);
        const email   = val(row,'ContactEmail'); if (email) parts.push(email);
    } else if (t==='project'){
        const v=val(row,'DeploymentVariant'); if (v) parts.push(v);
        const sap=val(row,'ProjectSAPID');    if (sap) parts.push('SAP '+sap);
    } else if (t==='site'){
        const fz=val(row,'FireZone'); if (fz) parts.push('Zone '+fz);
        const tc=val(row,'TenantCount'); if (tc!=null) parts.push(tc+' Tenants');
    } else if (t==='server'){
        ['ServerBrand','ServerOS','VirtualPlatform'].forEach(k=>{
            const v=val(row,k); if(v) parts.push(v);
        });
    } else if (t==='client'){
        ['ClientBrand','ClientOS'].forEach(k=>{
            const v=val(row,k); if(v) parts.push(v);
        });
    } else if (t==='radio'){
        const br=val(row,'RadioBrand'); if (br) parts.push(br);
        const md=val(row,'Mode'); if (md) parts.push(md);
        const ds=val(row,'DigitalStandard'); if (ds) parts.push(ds);
    } else if (t==='audio'){
        const br=val(row,'AudioDeviceBrand'); if (br) parts.push(br);
        const dir=val(row,'Direction'); if (dir) parts.push(dir);
    } else if (t==='phone'){
        const br=val(row,'PhoneBrand'); if (br) parts.push(br);
        const tp=val(row,'PhoneType'); if (tp) parts.push(tp);
    }
    return parts.join(' · ');
}
async function enrichRows(hits){
    const jobs = hits.map(async (h, i) => {
        try{
            const table = tableForType(h.type);
            const res = await fetch(`/row/${table}/${h.id}`);
            if (!res.ok) return;
            const row = await res.json();
            const info = formatPreview(h.type, row) || '';
            const cell = document.getElementById(`info-${i}`);
            if (cell) cell.textContent = info;
        } catch {}
    });
    await Promise.allSettled(jobs);
}

/* ===================== Event-Wiring ===================== */
function wireEvents() {
    // Hauptsuche (Button)
    searchBtn.onclick = () => runSearch(searchInput.value);

    // Enter → suchen | Tab → Top-Vorschlag übernehmen + sofort suchen
    searchInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
            runSearch(searchInput.value);
        } else if (e.key === 'Tab') {
            const completed = completeFromSuggestions();
            if (completed) {
                e.preventDefault();
                runSearch(searchInput.value);
            }
        }
    });

    // Auswahl eines datalist-Eintrags per Maus → automatisch suchen
    searchInput.addEventListener('change', () => {
        if ((searchInput.value || '').trim()) runSearch(searchInput.value);
    });

    // Autocomplete (debounced)
    searchInput.addEventListener('input', debounce(async () => {
        const q = (searchInput.value || '').trim();
        if (q.length < 2) { if (sugList) sugList.innerHTML = ''; return; }
        try {
            const res = await fetch(`${API.suggest}?q=${encodeURIComponent(q)}&max=8`);
            if (!res.ok) return;
            const arr = await res.json();
            if (sugList) sugList.innerHTML = arr.map(s => `<option value="${s}">`).join('');
        } catch {}
    }, 180));

    searchInput.addEventListener('input', refreshActiveTableMarkers);
    searchInput.addEventListener('change', refreshActiveTableMarkers);

    // Reindex (rechter Button)
    if (idxBtnSide) idxBtnSide.onclick = () => startReindex(idxBtnSide);

    // Fortschritt regelmäßig prüfen
    setInterval(pollProgress, 50);
    pollProgress();

    // Shortcuts initialisieren inkl. ARIA
    setupShortcuts();

    refreshActiveTableMarkers();
}
document.addEventListener('DOMContentLoaded', wireEvents);

// Top-Suggestion finden/übernehmen (case-insensitive); true = übernommen
function completeFromSuggestions(){
    if (!sugList) return false;
    const cur = (searchInput.value || '').trim().toLowerCase();
    if (!cur) return false;
    let best = null;
    [...sugList.options].some(opt => {
        const v = (opt.value || '').toLowerCase();
        if (v.startsWith(cur)) { best = opt.value; return true; }
        return false;
    });
    if (!best) return false;
    searchInput.value = best;
    return true;
}

// Startet den Reindex und stößt sofort ein Polling an
async function startReindex(btn) {
    if (btn) btn.disabled = true;
    try {
        await fetch(API.reindex, { method: 'POST' });
        await pollProgress();
    } catch (e) {
        console.error('Reindex-Start fehlgeschlagen', e);
    } finally {
        await sleep(150);
        if (btn) btn.disabled = false;
    }
}

/* ===================== Shortcuts ===================== */
function setupShortcuts() {
    document.querySelectorAll('.shortcut').forEach(sc => {
        const id        = sc.dataset.id;
        const headBtn   = sc.querySelector('.head');
        const labelEl   = sc.querySelector('.label');
        const renameBtn = sc.querySelector('.rename');
        const chevBtn   = sc.querySelector('.chev');
        const panel     = sc.querySelector('.panel');
        const listEl    = panel ? panel.querySelector('[data-role="list"]') : null;
        const hasList   = !!(sc.dataset.list && listEl);
        const inputEl   = hasList ? null : (panel ? panel.querySelector('input') : null);
        const defVal    = (sc.dataset.default || '').trim();

        const stLabelKey = `sc:${id}:label`;
        const stQueryKey = `sc:${id}:query`;
        labelEl.textContent = stGet(stLabelKey, labelEl.textContent);

        if (hasList) {
            if (listEl && !listEl.innerHTML.trim()) {
                listEl.innerHTML = '<p class="sc-status empty">Noch keine Daten geladen.</p>';
            }
        } else if (inputEl) {
            inputEl.value = stGet(stQueryKey, defVal);
            inputEl.placeholder = 'Lucene-Query';
            inputEl.setAttribute('aria-label','Lucene-Query');
        }

        if (panel) {
            const hid = `sc-head-${id}`;
            const pid = `sc-panel-${id}`;
            headBtn.id = hid;
            headBtn.setAttribute('aria-controls', pid);
            headBtn.setAttribute('aria-expanded', sc.classList.contains('open'));
            panel.id = pid;
            panel.setAttribute('role','region');
            panel.setAttribute('aria-labelledby', hid);
        }

        headBtn.addEventListener('click', (ev) => {
            if (ev.target === renameBtn || ev.target === chevBtn) return;
            const q = hasList ? defVal : (((inputEl && inputEl.value) || '').trim() || defVal);
            if (q) {
                if (!hasList) stSet(stQueryKey, q);
                runSearch(q);
            }
        });

        chevBtn.addEventListener('click', (ev) => {
            ev.stopPropagation();
            sc.classList.toggle('open');
            const open = sc.classList.contains('open');
            headBtn.setAttribute('aria-expanded', open);
            if (!open) return;
            if (hasList && listEl) {
                renderShortcutList(sc, listEl);
            } else if (inputEl) {
                inputEl.focus();
                inputEl.select();
            }
        });

        renameBtn.addEventListener('click', (ev) => {
            ev.stopPropagation();
            const current = labelEl.textContent.trim();
            const name = prompt('Neuer Name für den Shortcut:', current);
            if (name && name.trim()) {
                labelEl.textContent = name.trim();
                stSet(stLabelKey, labelEl.textContent);
            }
        });

        if (inputEl) {
            inputEl.addEventListener('keydown', (e) => {
                if (e.key === 'Enter') {
                    const q = inputEl.value.trim();
                    if (q) {
                        stSet(stQueryKey, q);
                        runSearch(q);
                    }
                } else if (e.key === 'Escape') {
                    sc.classList.remove('open');
                    headBtn.setAttribute('aria-expanded', false);
                }
            });

            inputEl.addEventListener('blur', () => stSet(stQueryKey, inputEl.value.trim()));
        }
    });
}

/* ===================== Suche ===================== */
function runSearch(raw){
    const prepared = buildUserQuery(raw);
    runLucene(prepared);
}

function shortUuid(value) {
    if (value === null || value === undefined) return '';
    const str = String(value);
    const uuidPattern = /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/;
    if (!uuidPattern.test(str)) return str;
    const idx = str.lastIndexOf('-');
    if (idx === -1 || idx === str.length - 1) return str;
    const lastSegment = str.slice(idx + 1);
    const trimmed = lastSegment.replace(/^0+/, '');
    return trimmed || '0';
}

function renderIdChip(value, displayOverride) {
    const fallback = (value === null || value === undefined) ? '' : String(value);
    let display = displayOverride;
    if (display === undefined || display === null || display === '') display = shortUuid(value);
    const text = display || fallback;
    if (!text) return '';
    return `<span class="id-chip">${escapeHtml(text)}</span>`;
}

function renderIdDisplay(value) {
    const fallback = (value === null || value === undefined) ? '' : String(value);
    const display = shortUuid(value);
    const chip = renderIdChip(value, display);
    if (chip) {
        return { inner: chip, title: fallback };
    }
    const safeText = escapeHtml(display || fallback);
    return { inner: safeText, title: fallback };
}

async function runLucene(q) {
    const query = (q ?? '').trim();
    if (!query) return;
    try {
        setBusy(resultArea, true);
        const res  = await fetch(`${API.search}?q=${encodeURIComponent(query)}`);
        const hits = await res.json();
        if (!Array.isArray(hits) || !hits.length) {
            resultArea.textContent = '(keine Treffer)';
            return;
        }

        const rows = hits.map((h, i) => {
            const snippet = (h.snippet ?? '').trim();
            const snippetHtml = snippet ? `<div class="hit-snippet"><small>${escapeHtml(snippet)}</small></div>` : '';
            const typeArg = JSON.stringify(h.type ?? '');
            const idArg = JSON.stringify(h.id ?? '');
            const idDisplay = renderIdDisplay(h.id);
            return `
      <tr onclick='toDetails(${typeArg},${idArg})' style="cursor:pointer">
        <td>${escapeHtml(h.type)}</td>
        <td title="${escapeHtml(idDisplay.title)}">${idDisplay.inner}</td>
        <td><div class="hit-text">${escapeHtml(h.text ?? '')}</div>${snippetHtml}</td>
        <td id="info-${i}"></td>
      </tr>`;
        }).join('');

        resultArea.innerHTML = `
      <div class="table-scroll">
        <table>
          <tr><th>Typ</th><th>ID</th><th>Text / Snippet</th><th>Info</th></tr>
          ${rows}
        </table>
      </div>`;

        enrichRows(hits);

    } catch (e) {
        resultArea.innerHTML = `<p id="error" role="alert">Fehler: ${e}</p>`;
    } finally {
        setBusy(resultArea, false);
    }
}

/* Tabellen-Viewer (100-Zeilen-Preview) */
function renderTableCell(tableName, columnName, value) {
    const key = (columnName === undefined || columnName === null) ? '' : String(columnName);
    const raw = (value === undefined || value === null) ? '' : String(value);
    const isIdColumn = /(id|guid)$/i.test(key);
    const fragments = getColumnFragments(tableName, key);
    const hasFragments = fragments.length > 0;
    let innerHtml = escapeHtml(raw);
    if (isIdColumn && raw) {
        const rendered = renderIdDisplay(value);
        const titleAttr = escapeHtml(rendered.title);
        innerHtml = `<span class="cell-id" title="${titleAttr}">${rendered.inner}</span>`;
    }
    if (!hasFragments) {
        return `<td>${innerHtml}</td>`;
    }
    const options = fragments.map((opt, idx) => {
        const label = opt.label || opt.query;
        const query = formatFragment(opt.query);
        const description = opt.description ? `<small>${escapeHtml(opt.description)}</small>` : '';
        return `
            <button type="button" role="menuitem" class="table-fragment-option" data-fragment='${escapeHtml(query)}' data-option-idx="${idx}">
                <span class="table-fragment-option-label">${escapeHtml(label)}</span>
                ${description}
            </button>`;
    }).join('');
    const safeColumn = escapeHtml(key);
    const cellLabel = key ? `Filteroptionen für ${key}` : 'Filteroptionen';
    return `
        <td class="has-fragment-options">
            <div class="cell-value" role="button" tabindex="0" aria-label="${escapeHtml(cellLabel)}">
                <span class="cell-value-text">${innerHtml}</span>
                <span class="cell-value-hint" aria-hidden="true">⋯</span>
            </div>
            <div class="cell-popover" role="menu" aria-label="Filteroptionen für ${safeColumn}">
                <p class="cell-popover-title">${safeColumn}</p>
                <div class="cell-popover-options">${options}</div>
            </div>
        </td>`;
}

async function showTable(name) {
    try {
        setBusy(resultArea, true);
        const res  = await fetch(`${API.table}/${encodeURIComponent(name)}`);
        const rows = await res.json();
        if (!Array.isArray(rows) || !rows.length) { resultArea.textContent = '(leer)'; return; }

        const cols = Object.keys(rows[0]);
        const hdr  = cols.map(c => `<th>${c}</th>`).join('');
        const body = rows.map(r => `<tr>${cols.map(c => renderTableCell(name, c, r[c])).join('')}</tr>`).join('');
        const typeFragment = `type:${name}`;

        resultArea.innerHTML =
            `<div class="table-title-bar">
                <button type="button" class="table-title-button" data-fragment="${escapeHtml(typeFragment)}" title="Filter ${escapeHtml(typeFragment)} ausführen">${escapeHtml(name)}</button>
                <span class="table-title-hint">Klick = Filter setzen</span>
            </div>
        <div class="table-scroll">
          <table>
            <tr>${hdr}</tr>${body}
         </table>
       </div>`;

        refreshActiveTableMarkers();
    } catch (e) {
        resultArea.innerHTML = `<p id="error" role="alert">Fehler: ${e}</p>`;
    } finally {
        setBusy(resultArea, false);
    }
}

/* Details-Navigation (details.html) */
function toDetails(type, id) {
    location.href = `/details.html?type=${encodeURIComponent(type)}&id=${id}`;
}
window.toDetails = toDetails;

/* ===================== Fortschrittsanzeige ===================== */
async function pollProgress() {
    try {
        const r = await fetch(API.progress);
        if (!r.ok) throw new Error(`HTTP ${r.status}`);
        const p = await r.json(); // IndexProgress.Status

        const running = !!p.active;
        const pct     = Math.max(0, Math.min(100, p.percent || 0));
        const done    = p.totalDone ?? 0;
        const total   = p.grandTotal ?? 0;

        if (running || (done < total)) {
            idxBox.classList.add('active');
            idxBar.style.width = pct.toFixed(1) + '%';
            idxBar.setAttribute('aria-valuenow', pct.toFixed(0));
            idxBox.setAttribute('aria-busy','true');
            idxText.textContent = `Index: ${pct.toFixed(0)}% (${done}/${total})`;
        } else {
            idxBox.classList.remove('active');
            idxBar.style.width = '0%';
            idxBar.setAttribute('aria-valuenow', '0');
            idxBox.removeAttribute('aria-busy');
            idxText.textContent = '';
        }
    } catch {
        // still bleiben, wenn Endpunkt (noch) nicht erreichbar ist
    }
}