import React, {useEffect, useState} from "react";
import SearchBar from "../SearchBar";
import ResultArea from "../ResultArea";
import IndexProgressBar from "../IndexProgressBar";
import type {IndexProgress} from "../../types";

const Content:React.FC = ()=>{
    const [idx,setIdx]=useState<IndexProgress>({running:false, percent:0});

    // Einfaches Polling, falls Backend Fortschritt bereitstellt (/index-status)
    useEffect(()=>{
        let t:ReturnType<typeof setInterval> | null = null;
        const poll = async ()=>{
            try{
                const r = await fetch("/index-status");
                if(!r.ok) return;
                const s = await r.json() as {running:boolean; percent:number; label?:string};
                setIdx(s);
            }catch{/* ignore */}
        };
        t = setInterval(poll, 1000);
        poll();
        return ()=>{ if(t) clearInterval(t); };
    },[]);

    const onSearch = (query:string)=>{
        window.dispatchEvent(new CustomEvent("lifex:search",{detail:{query}}));
    };

    return (
        <div>
            <IndexProgressBar state={idx}/>
            <SearchBar onSearch={onSearch}/>
            <ResultArea/>
        </div>
    );
};
export default Content;