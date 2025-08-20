import React from "react";

type Props = {
    onStart:(label?:string)=>void;
};

const ReindexButton:React.FC<Props> = ({onStart})=>{
    const click = async ()=>{
        onStart("Starting …");
        try{
            // optional: Backend-Trigger (falls vorhanden)
            await fetch("/reindex", {method:"POST"}).catch(()=>{});
        }finally{
            // Polling durch Content übernimmt die Fortschrittsanzeige
        }
    };
    return <button className="btn btn-primary" onClick={click}>Neu indexieren</button>;
};
export default ReindexButton;