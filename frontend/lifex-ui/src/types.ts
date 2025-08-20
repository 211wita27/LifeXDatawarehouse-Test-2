export type SearchHit = { type:string; id:number; text:string; extra?:string };
export type TableDump = { columns:string[]; rows:Array<Record<string,unknown>> };

export type IndexProgress = { running:boolean; percent:number; label?:string };

export type TableName =
    | "Account" | "Project" | "Site" | "Server"
    | "WorkingPosition" | "Radio" | "AudioDevice" | "PhoneIntegration";