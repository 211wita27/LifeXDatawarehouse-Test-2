import React, {useEffect, useMemo, useRef, useState} from "react";
import {expandToPrefix, isLucene} from "../../utils";

type Props = { onSearch:(q:string)=>void };

const SearchBar:React.FC<Props> = ({onSearch})=>{
    const [q,setQ]=useState("");
    const [sug,setSug]=useState<string[]>([]);
    const busyRef = useRef<ReturnType<typeof setTimeout> | null>(null);

    // Autocomplete (Fallback: keine Fehler, wenn /suggest fehlt)
    useEffect(()=>{
        if(q.trim().length<2){ setSug([]); return; }
        if(busyRef.current) clearTimeout(busyRef.current);
        busyRef.current = setTimeout(async ()=>{
            try{
                const r = await fetch(`/suggest?q=${encodeURIComponent(q)}`);
                if(r.ok){ setSug(await r.json()); }
                else setSug([]);
            }catch{ setSug([]); }
        }, 180);
    },[q]);

    const top = useMemo(()=>sug[0] ?? "", [sug]);

    const run = (force?:string)=>{
        const raw = (force ?? q).trim();
        if(!raw) return;
        const finalQ = isLucene(raw) ? raw : expandToPrefix(raw);
        onSearch(finalQ);
    };

    return (
        <div className="input-group mb-3" role="search">
            <input
                className="form-control"
                placeholder="Global-Suche (Lucene oder normal)"
                list="lifex-sug" autoComplete="off"
                value={q} onChange={e=>setQ(e.target.value)}
                onKeyDown={e=>{
                    if(e.key==="Enter") run();
                    if(e.key==="Tab" && top){ e.preventDefault(); run(top); }
                }}
            />
            <button className="btn btn-outline-secondary" onClick={()=>run()}>Suchen</button>
            <datalist id="lifex-sug">
                {sug.map((s,i)=>(<option value={s} key={i}/>))}
            </datalist>
        </div>
    );
};
export default SearchBar;