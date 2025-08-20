import React from "react";
import ShortcutList from "../ShortcutList";
import TableList from "../TableList";

type Props={};
const Sidebar:React.FC<Props> = ()=>{
    const run = (query:string)=>{
        const ev = new CustomEvent("lifex:search",{ detail:{ query } });
        window.dispatchEvent(ev);
    };
    const show = (name: string)=>{
        const ev = new CustomEvent("lifex:showTable",{ detail:{ name } });
        window.dispatchEvent(ev);
    };

    return (
        <div>
            <ShortcutList onRun={run}/>
            <TableList onShow={show as any}/>
        </div>
    );
};
export default Sidebar;