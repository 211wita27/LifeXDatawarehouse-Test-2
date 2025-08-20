import React from "react";
import ShortcutItem from "../ShortcutItem";

type Props={ onRun:(q:string)=>void };

const presets:[string,string][]= [
    ["Alle Accounts","type:account"],
    ["Große Projekte","type:project AND (CustomL OR CustomXL)"],
    ["Accounts 🇩🇪","type:account AND country:germany"],
    ["Sites – Lenovo-Server","type:site AND serverBrand:lenovo"],
    ["(frei 1)",""]
];

const ShortcutList:React.FC<Props> = ({onRun})=>{
    return (
        <section aria-labelledby="sc-title">
            <h2 id="sc-title" className="h5">Shortcuts</h2>
            {presets.map(([label, q]) => (
                <ShortcutItem key={label} label={label} defaultQuery={q} onRun={onRun}/>
            ))}
        </section>
    );
};
export default ShortcutList;