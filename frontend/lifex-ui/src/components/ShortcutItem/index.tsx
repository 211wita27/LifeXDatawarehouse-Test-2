import React, {useId, useState} from "react";

type Props = {
    label:string;
    defaultQuery:string;
    onRun:(query:string)=>void;
};

const ShortcutItem:React.FC<Props> = ({label, defaultQuery, onRun})=>{
    const [open,setOpen]=useState(false);
    const [value,setValue]=useState(defaultQuery);
    const hid=useId();

    return (
        <div className="card shadow-sm mb-2 border-0">
            <button
                className="btn btn-primary w-100 d-flex align-items-center justify-content-between"
                aria-expanded={open} aria-controls={hid}
                onClick={()=>setOpen(v=>!v)}
            >
                <span className="fw-semibold">{label}</span>
                <span className="ms-2">{open ? "▾" : "▸"}</span>
            </button>

            <div id={hid} hidden={!open} className="p-2" role="region" aria-labelledby={hid+"-lbl"}>
                <label id={hid+"-lbl"} className="form-label small text-muted">Lucene-Query (Override)</label>
                <input className="form-control" value={value} onChange={e=>setValue(e.target.value)} />
                <div className="d-flex gap-2 mt-2">
                    <button className="btn btn-primary" onClick={()=>onRun(value)}>Ausführen</button>
                    <button className="btn btn-outline-secondary" onClick={()=>setValue(defaultQuery)}>Reset</button>
                </div>
            </div>
        </div>
    );
};
export default ShortcutItem;