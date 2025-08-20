import React from "react";
import type {TableName} from "../../types";

type Props={ onShow:(name:TableName)=>void };

const names:TableName[] = [
    "Account","Project","Site","Server","WorkingPosition",
    "Radio","AudioDevice","PhoneIntegration"
];

const TableList:React.FC<Props> = ({onShow})=>{
    return (
        <section className="mt-3" aria-labelledby="tbl-title">
            <h2 id="tbl-title" className="h5">Tabellen</h2>
            <ul className="list-unstyled m-0">
                {names.map(n => (
                    <li key={n}>
                        <button className="btn btn-light w-100 text-start mb-2" onClick={()=>onShow(n)}>{n}</button>
                    </li>
                ))}
            </ul>
        </section>
    );
};
export default TableList;