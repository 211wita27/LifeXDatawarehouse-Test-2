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
const idxBtn  = document.getElementById('idx-reindex');
const idxBtnSide = document.getElementById('idx-reindex-side');

const sugList = document.getElementById('sug');

/* ===================== Utils ===================== */
const sleep = (ms) => new Promise(r => setTimeout(r, ms));
const debounce = (fn, ms=250) => { let t; return (...a)=>{clearTimeout(t); t=setTimeout(()=>fn(...a),ms);} };
const stGet = (k, d) => { try { const v = localStorage.getItem(k); return v === null ? d : v; } catch { return d; } };
const stSet = (k, v) => { try { localStorage.setItem(k, v); } catch {} };

/* ===================== Event-Wiring ===================== */
function wireEvents() {
    // Hauptsuche
    searchBtn.onclick = () => runLucene(searchInput.value);
    searchInput.addEventListener('keydown', e => { if (e.key === 'Enter') runLucene(searchInput.value); });

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

    // Reindex-Buttons (oben im Balken + rechts im Hilfe-Panel)
    if (idxBtn)     idxBtn.onclick     = () => startReindex(idxBtn);
    if (idxBtnSide) idxBtnSide.onclick = () => startReindex(idxBtnSide);

    // Fortschritt regelmäßig prüfen
    setInterval(pollProgress, 900);
    pollProgress();

    // Shortcuts initialisieren
    setupShortcuts();
}

document.addEventListener('DOMContentLoaded', wireEvents);

// Startet den Reindex und stößt sofort ein Polling an
async function startReindex(btn) {
    if (btn) btn.disabled = true;
    try {
        await fetch(API.reindex, { method: 'POST' });
        await pollProgress(); // Fortschritt direkt anzeigen
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
        const headBtn   = sc.querySelector('.head');   // blauer Button
        const labelEl   = sc.querySelector('.label');
        const renameBtn = sc.querySelector('.rename');
        const chevBtn   = sc.querySelector('.chev');   // Pfeil
        const inputEl   = sc.querySelector('.panel input');
        const defVal    = (sc.dataset.default || '').trim();

        // Persistierte Werte laden
        const stLabelKey = `sc:${id}:label`;
        const stQueryKey = `sc:${id}:query`;
        labelEl.textContent = stGet(stLabelKey, labelEl.textContent);
        inputEl.value       = stGet(stQueryKey, defVal);
        inputEl.placeholder = 'Lucene-Query';

        // Klick auf den blauen Button → Query ausführen
        headBtn.addEventListener('click', (ev) => {
            if (ev.target === renameBtn || ev.target === chevBtn) return; // Stift/Pfeil ignorieren
            const q = (inputEl.value || '').trim() || defVal;
            if (q) {
                stSet(stQueryKey, q);
                runLucene(q);
            }
        });

        // Pfeil: nur auf/zu klappen
        chevBtn.addEventListener('click', (ev) => {
            ev.stopPropagation();
            sc.classList.toggle('open');
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
                    runLucene(q);
                }
            } else if (e.key === 'Escape') {
                sc.classList.remove('open');
            }
        });

        inputEl.addEventListener('blur', () => stSet(stQueryKey, inputEl.value.trim()));
    });
}

/* ===================== Suche ===================== */
async function runLucene(q) {
    const query = (q ?? '').trim();
    if (!query) return;
    try {
        const res  = await fetch(`${API.search}?q=${encodeURIComponent(query)}`);
        const hits = await res.json();
        if (!Array.isArray(hits) || !hits.length) {
            resultArea.textContent = '(keine Treffer)';
            return;
        }

        const rows = hits.map(h => `
      <tr onclick="toDetails('${h.type}',${h.id})" style="cursor:pointer">
        <td>${h.type}</td>
        <td>${h.id}</td>
        <td>${h.text ?? ''}</td>
      </tr>`).join('');

        resultArea.innerHTML = `
      <div class="table-scroll">
        <table>
          <tr><th>Typ</th><th>ID</th><th>Text</th></tr>
          ${rows}
        </table>
      </div>`;
    } catch (e) {
        resultArea.innerHTML = `<p id="error">Fehler: ${e}</p>`;
    }
}

/* Tabellen-Viewer (100-Zeilen-Preview) */
async function showTable(name) {
    try {
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
        resultArea.innerHTML = `<p id="error">Fehler: ${e}</p>`;
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
            idxText.textContent = `Index: ${pct.toFixed(0)}% (${done}/${total})`;
        } else {
            idxBox.classList.remove('active');
            idxBar.style.width = '0%';
            idxText.textContent = '';
        }
    } catch {
        // still bleiben, wenn Endpunkt (noch) nicht erreichbar ist
    }
}