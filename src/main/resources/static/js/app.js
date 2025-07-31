/* ====================================================================
   Hilfsreferenzen
==================================================================== */
const resultArea  = document.getElementById('resultArea');
const searchInput = document.getElementById('search-input');

/* ====================================================================
   Globale Suche   (Enter-Taste & Button)
==================================================================== */
document.getElementById('search-btn').onclick = ()=>runLucene(searchInput.value);
searchInput.addEventListener('keydown',e=>{
    if(e.key==='Enter'){ runLucene(searchInput.value); }
});

async function runLucene(q){
    const query=q.trim(); if(!query){return;}
    resultArea.innerHTML='';                       // nichts Unprofessionelles :-)

    try{
        const res=await fetch(`/accounts/lucene-search?query=${encodeURIComponent(query)}`);
        const data=await res.json();
        if(!data.length){ resultArea.textContent='(keine Treffer)'; return;}

        const rows=data.map(a=>`
        <tr><td>${a.accountID}</td><td>${a.accountName}</td><td>${a.country}</td></tr>`).join('');
        resultArea.innerHTML=`
       <div class="table-scroll">
         <table>
           <tr><th>ID</th><th>Name</th><th>Land</th></tr>${rows}
         </table>
       </div>`;
    }catch(e){ resultArea.innerHTML=`<p id="error">Fehler (${e.message})</p>`;}
}

/* ====================================================================
   Tabellenanzeige (max 100 Zeilen)
==================================================================== */
async function showTable(name){
    resultArea.innerHTML='';
    try{
        const res=await fetch(`/table/${name}`);
        const rows=await res.json();
        if(!rows.length){resultArea.textContent='(leer)';return;}

        const cols=Object.keys(rows[0]);
        const head=cols.map(c=>`<th>${c}</th>`).join('');
        const body=rows.map(r=>`<tr>${cols.map(c=>`<td>${r[c]}</td>`).join('')}</tr>`).join('');
        resultArea.innerHTML=`
        <h2>${name}</h2>
        <div class="table-scroll">
          <table><tr>${head}</tr>${body}</table>
        </div>`;
    }catch(e){ resultArea.innerHTML=`<p id="error">Fehler (${e.message})</p>`;}
}

/* ====================================================================
   Shortcuts
==================================================================== */
document.querySelectorAll('.shortcut').forEach(box=>{
    const id      = box.dataset.id;
    const inp     = box.querySelector('input');
    const labelEl = box.querySelector('.label');
    const chev    = box.querySelector('.chevron');
    const rename  = box.querySelector('.rename');
    const headBtn = box.querySelector('.head');

    /* gespeicherte Query laden */
    const savedQ = localStorage.getItem(id) ?? box.dataset.default;
    inp.value = savedQ;

    /* gespeicherte Label laden */
    const savedL = localStorage.getItem(id+'-label');
    if(savedL) labelEl.textContent = savedL;

    /* — Klick auf Chevron = auf/zu — */
    chev.addEventListener('click',e=>{
        e.stopPropagation();
        box.classList.toggle('open');
    });

    /* — Klick auf Rename-Icon — */
    rename.addEventListener('click',e=>{
        e.stopPropagation();
        const neu = prompt('Neuer Button-Name?', labelEl.textContent);
        if(neu){
            labelEl.textContent = neu;
            localStorage.setItem(id+'-label', neu);
        }
    });

    /* — Klick auf Head (ohne Chevron/Rename) = Query ausführen — */
    headBtn.addEventListener('click',e=>{
        if(e.target!==headBtn){return;}      // Chevron oder Rename ignorieren
        const query=inp.value.trim();
        if(query){
            runLucene(query);
        }
    });

    /* — Query ändern → speichern — */
    inp.addEventListener('change',()=>localStorage.setItem(id, inp.value.trim()));
});