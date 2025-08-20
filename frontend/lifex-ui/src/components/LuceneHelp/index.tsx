import React from "react";

const LuceneHelp:React.FC = ()=>(
    <div>
        <h2 className="h5">Lucene Syntax</h2>
        <ul className="small">
            <li><code>tech*</code> Wildcard</li>
            <li><code>"green valley"</code> Phrase</li>
            <li><code>+foo -bar</code> Muss / Nicht</li>
            <li><code>country:germany</code> Feldsuche</li>
            <li>Project: <code>type:project AND CustomL</code></li>
        </ul>
    </div>
);
export default LuceneHelp;