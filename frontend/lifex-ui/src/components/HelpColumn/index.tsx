import React, {useState} from "react";
import ReindexButton from "../ReindexButton";
import LuceneHelp from "../LuceneHelp";

const HelpColumn:React.FC = ()=>{
    const [note,setNote]=useState<string>("");

    const start = (lbl?:string)=>{
        setNote(lbl ?? "Neuindex gestartet …");
    };

    return (
        <div className="card shadow-sm p-3">
            <LuceneHelp />
            <hr/>
            <h3 className="h6">Index-Verwaltung</h3>
            <ReindexButton onStart={start}/>
            {note && <p className="text-muted small mt-2">{note}</p>}
            <p className="small text-muted mt-2">Fortschritt erscheint als Balken im Hauptbereich.</p>
        </div>
    );
};
export default HelpColumn;