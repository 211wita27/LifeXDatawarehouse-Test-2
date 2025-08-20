import React from "react";
import type { IndexProgress } from '../../types';

const IndexProgressBar:React.FC<{state:IndexProgress}> = ({state})=>{
    if(!state.running) return null;
    return (
        <div className="d-flex align-items-center gap-2 my-2" aria-live="polite">
            <div className="progress flex-grow-1" role="progressbar" aria-valuenow={state.percent} aria-valuemin={0} aria-valuemax={100}>
                <div className="progress-bar" style={{width:`${state.percent}%`}} />
            </div>
            <div className="small text-muted" style={{minWidth:160, textAlign:"right"}}>{state.label ?? `${state.percent}%`}</div>
        </div>
    );
};
export default IndexProgressBar;