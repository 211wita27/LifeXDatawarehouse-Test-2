/* ========== DOM-Referenzen ========== */
const resultArea  = document.getElementById('resultArea');
const searchInput = document.getElementById('search-input');
document.getElementById('search-btn').onclick = () => runLucene(searchInput.value);
searchInput.addEventListener('keydown', e => { if (e.key === 'Enter') runLucene(searchInput.value); });

/* ========== Lucene – globale Suche ========== */
async function runLucene(q) {
    const query = q.trim();
    if (!query) return;
    try {
        const res  = await fetch('/search?q=' + encodeURIComponent(query));
        const hits = await res.json();
        if (!hits.length) { resultArea.textContent = '(keine Treffer)'; return; }

        const rows = hits.map(h => `
            <tr onclick="toDetails('${h.type}',${h.id})">
              <td>${h.type}</td><td>${h.id}</td><td>${h.text}</td>
            </tr>`).join('');

        resultArea.innerHTML = `
          <div class="table-scroll">
            <table>
              <tr><th>Typ</th><th>ID</th><th>Text</th></tr>${rows}
            </table>
          </div>`;
    } catch (e) {
        resultArea.innerHTML = `<p id="error">Fehler ${e}</p>`;
    }
}

/* ========== Tabellen-Listing ========== */
async function showTable(name) {
    try {
        const res  = await fetch('/table/' + name);
        const rows = await res.json();
        if (!rows.length) { resultArea.textContent = '(leer)'; return; }

        const cols = Object.keys(rows[0]);
        const hdr  = cols.map(c => `<th>${c}</th>`).join('');
        const body = rows.map(r =>
            `<tr>${cols.map(c => `<td>${r[c]}</td>`).join('')}</tr>`).join('');

        resultArea.innerHTML = `
          <h2>${name}</h2><div class="table-scroll">
            <table><tr>${hdr}</tr>${body}</table></div>`;
    } catch (e) {
        resultArea.innerHTML = `<p id="error">Fehler ${e}</p>`;
    }
}

/* ========== Shortcut-Initialisierung ========== */
document.querySelectorAll('.shortcut').forEach(box => {
    const id   = box.dataset.id,
        inp  = box.querySelector('input'),
        lbl  = box.querySelector('.label'),
        chev = box.querySelector('.chev');

    /* gespeicherte Query laden */
    inp.value = localStorage.getItem(id) ?? box.dataset.default;

    /* gespeicherten Label-Text laden */
    const lblStored = localStorage.getItem(id + '-label');
    if (lblStored) lbl.textContent = lblStored;

    /* Pfeil auf/zu */
    chev.onclick = e => { e.stopPropagation(); box.classList.toggle('open'); };

    /* Shortcut ausführen */
    box.querySelector('.head').addEventListener('click', e => {
        if (e.target.classList.contains('chev') ||
            e.target.classList.contains('rename')) return;
        runLucene(inp.value.trim());
    });

    /* Label umbenennen */
    box.querySelector('.rename').onclick = e => {
        e.stopPropagation();
        const neu = prompt('Neuer Name?', lbl.textContent);
        if (neu) { lbl.textContent = neu; localStorage.setItem(id + '-label', neu); }
    };

    /* Query speichern */
    inp.onchange = () => localStorage.setItem(id, inp.value.trim());
});

/* ========== Details-Navigation ========== */
function toDetails(type, id) {
    location.href = `/details.html?type=${type}&id=${id}`;
}