import React, {useEffect, useState} from "react";
import type {SearchHit, TableDump} from "../../types";
import {getJSON, mapTypeToTable} from "../../utils";

type View =
    | { kind:"idle" }
    | { kind:"loading"; text:string }
    | { kind:"error"; text:string }
    | { kind:"hits"; hits:SearchHit[] }
    | { kind:"table"; name:string; dump:TableDump };

const ResultArea:React.FC = ()=>{
    const [view,setView]=useState<View>({kind:"idle"});

    // global Events aus Sidebar / SearchBar
    useEffect(()=>{
        const onSrch = async (e:Event)=>{
            const q = (e as CustomEvent).detail?.query as string;
            if(!q) return;
            setView({kind:"loading", text:"Suche läuft …"});
            try{
                const hits = await getJSON<SearchHit[]>(`/search?q=${encodeURIComponent(q)}`);
                setView(hits.length ? {kind:"hits", hits} : {kind:"error", text:`Keine Treffer für “${q}”.`});
            }catch(err:any){
                setView({kind:"error", text: err?.message || "Suche nicht möglich."});
            }
        };
        const onTbl = async (e:Event)=>{
            const name = (e as CustomEvent).detail?.name as string;
            if(!name) return;
            setView({kind:"loading", text:`Lade Tabelle ${name} …`});
            try{
                const dump = await getJSON<TableDump>(`/table/${encodeURIComponent(name)}`);
                setView({kind:"table", name, dump});
            }catch(err:any){
                setView({kind:"error", text: err?.message || "Tabelle konnte nicht geladen werden."});
            }
        };
        window.addEventListener("lifex:search", onSrch);
        window.addEventListener("lifex:showTable", onTbl);
        return ()=> {
            window.removeEventListener("lifex:search", onSrch);
            window.removeEventListener("lifex:showTable", onTbl);
        };
    },[]);

    if(view.kind==="idle"){
        return <div className="alert alert-secondary">Nutze die Suchleiste oder die Bedienelemente links.</div>;
    }
    if(view.kind==="loading"){
        return <div aria-busy="true" className="text-muted">{view.text}</div>;
    }
    if(view.kind==="error"){
        return <div className="alert alert-danger" role="alert">{view.text}</div>;
    }
    if(view.kind==="hits"){
        return (
            <div className="table-responsive">
                <table className="table table-sm table-striped">
                    <thead><tr><th>Typ</th><th>ID</th><th>Text</th><th>Info</th></tr></thead>
                    <tbody>
                    {view.hits.map((h,i)=>(
                        <tr key={i}>
                            <td>{h.type}</td>
                            <td>
                                <a href={`/details.html?type=${encodeURIComponent(h.type)}&id=${encodeURIComponent(h.id)}`}>
                                    {h.id}
                                </a>
                            </td>
                            <td>{h.text}</td>
                            <td>{h.extra ?? ""}</td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        );
    }
    // table
    return (
        <>
            <h2 className="h5 mb-2">{view.name} – Vorschau (100)</h2>
            <div className="table-responsive">
                <table className="table table-sm table-bordered">
                    <thead>
                    <tr>{view.dump.columns.map(c=><th key={c} scope="col">{c}</th>)}</tr>
                    </thead>
                    <tbody>
                    {view.dump.rows.map((r,ri)=>(
                        <tr key={ri}>
                            {view.dump.columns.map(c=>{
                                const v=r[c];
                                // falls es wie im Alt-UI id+type gibt, linke auf Details
                                if(/id$/i.test(c)){
                                    const id = Number(v);
                                    if(!Number.isNaN(id)){
                                        const typeGuess = view.name; // grobe Zuordnung
                                        const table = mapTypeToTable(typeGuess);
                                        return (
                                            <td key={c}>
                                                <a href={`/details.html?type=${encodeURIComponent(table)}&id=${id}`}>{String(v)}</a>
                                            </td>
                                        );
                                    }
                                }
                                return <td key={c}>{String(v??"")}</td>;
                            })}
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        </>
    );
};
export default ResultArea;