export const isLucene = (q:string) =>
    /[:\+\-\(\)\*"]/.test(q); // grobe Heuristik wie im Alt-Frontend

export const expandToPrefix = (q:string) =>
    q.split(/\s+/).map(t => t.trim()).filter(Boolean)
        .map(t => /[\*\:\+\-\(\)"]/.test(t) ? t : `${t}*`).join(" ");

export async function getJSON<T>(url:string):Promise<T>{
    const r = await fetch(url);
    if(!r.ok) throw new Error(await r.text().catch(()=>r.statusText));
    return r.json();
}

export async function postJSON<T=unknown>(url:string, body:unknown):Promise<T>{
    const r = await fetch(url,{ method:"POST", headers:{ "Content-Type":"application/json" }, body: JSON.stringify(body) });
    if(!r.ok) throw new Error(await r.text().catch(()=>r.statusText));
    return r.json().catch(()=> ({} as T));
}

export function mapTypeToTable(t:string){
    const k=t.toLowerCase();
    if(k==="client") return "WorkingPosition";
    if(k==="audio") return "AudioDevice";
    if(k==="phone") return "PhoneIntegration";
    return t.charAt(0).toUpperCase()+t.slice(1);
}