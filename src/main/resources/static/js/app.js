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

    // Reindex (rechter Button)
    if (idxBtnSide) idxBtnSide.onclick = () => startReindex(idxBtnSide);

    // Fortschritt regelmäßig prüfen
    setInterval(pollProgress, 50);
    pollProgress();

    // Shortcuts initialisieren inkl. ARIA
    setupShortcuts();
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
        const inputEl   = sc.querySelector('.panel input');
        const defVal    = (sc.dataset.default || '').trim();

        // Persistierte Werte laden
        const stLabelKey = `sc:${id}:label`;
        const stQueryKey = `sc:${id}:query`;
        labelEl.textContent = stGet(stLabelKey, labelEl.textContent);
        inputEl.value       = stGet(stQueryKey, defVal);
        inputEl.placeholder = 'Lucene-Query';
        inputEl.setAttribute('aria-label','Lucene-Query');

        // ARIA-Beziehungen & Zustände
        const hid = `sc-head-${id}`;
        const pid = `sc-panel-${id}`;
        headBtn.id = hid;
        headBtn.setAttribute('aria-controls', pid);
        headBtn.setAttribute('aria-expanded', sc.classList.contains('open'));
        const panel = sc.querySelector('.panel');
        panel.id = pid;
        panel.setAttribute('role','region');
        panel.setAttribute('aria-labelledby', hid);

        // Klick auf den blauen Button → Query ausführen
        headBtn.addEventListener('click', (ev) => {
            if (ev.target === renameBtn || ev.target === chevBtn) return;
            const q = (inputEl.value || '').trim() || defVal;
            if (q) {
                stSet(stQueryKey, q);
                runSearch(q);
            }
        });

        // Pfeil: nur auf/zu klappen
        chevBtn.addEventListener('click', (ev) => {
            ev.stopPropagation();
            sc.classList.toggle('open');
            headBtn.setAttribute('aria-expanded', sc.classList.contains('open'));
            if (sc.classList.contains('open')) {
                inputEl.focus();
                inputEl.select();
            }
        });

        // Umbenennen (✏️)
        renameBtn.addEventListener('click', (ev) => {
            ev.stopPropagation();
            const current = labelEl.textContent.trim();
            const name = prompt('Neuer Name für den Shortcut:', current);
            if (name && name.trim()) {
                labelEl.textContent = name.trim();
                stSet(stLabelKey, labelEl.textContent);
            }
        });

        // Enter im Eingabefeld => Suche ausführen; Esc => Panel schließen
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
    });
}

/* ===================== Suche ===================== */
function runSearch(raw){
    const prepared = buildUserQuery(raw);
    runLucene(prepared);
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
            return `
      <tr onclick="toDetails('${h.type}',${h.id})" style="cursor:pointer">
        <td>${escapeHtml(h.type)}</td>
        <td>${escapeHtml(String(h.id ?? ''))}</td>
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
async function showTable(name) {
    try {
        setBusy(resultArea, true);
        const res  = await fetch(`${API.table}/${encodeURIComponent(name)}`);
        const rows = await res.json();
        if (!Array.isArray(rows) || !rows.length) { resultArea.textContent = '(leer)'; return; }

        const cols = Object.keys(rows[0]);
        const hdr  = cols.map(c => `<th>${c}</th>`).join('');
        const body = rows.map(r => `<tr>${cols.map(c => `<td>${r[c]}</td>`).join('')}</tr>`).join('');

        resultArea.innerHTML =
            `<h2>${name}</h2>
       <div class="table-scroll">
         <table>
           <tr>${hdr}</tr>${body}
         </table>
       </div>`;
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