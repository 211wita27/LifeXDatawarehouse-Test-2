(function(global){
    const pick = (obj, ...keys) => {
        if(!obj) return undefined;
        const entries = Object.keys(obj);
        for(const key of keys){
            if(key == null) continue;
            if(obj[key] !== undefined && obj[key] !== null) return obj[key];
            const match = entries.find(k => k.toLowerCase() === String(key).toLowerCase());
            if(match && obj[match] !== undefined && obj[match] !== null) return obj[match];
        }
        return undefined;
    };

    const formatLabel = (name, value) => {
        if(!value) return '';
        if(!name) return value;
        const short = String(value).split('-')[0];
        return `${name} (${short})`;
    };

    const asyncSources = {
        accounts:{
            url:'/accounts',
            map:item=>{
                const value = pick(item,'accountID','accountId','AccountID');
                const name = pick(item,'accountName','AccountName');
                return value ? {value, label: formatLabel(name,value)} : null;
            }
        },
        addresses:{
            url:'/addresses',
            map:item=>{
                const value = pick(item,'addressID','addressId','AddressID');
                const street = pick(item,'street','Street');
                const city = pick(item,'cityID','CityID');
                const label = street || city ? `${[street,city].filter(Boolean).join(', ')} (${String(value).split('-')[0]})` : value;
                return value ? {value, label, cityID: pick(item,'cityID','CityID')} : null;
            }
        },
        projects:{
            url:accountId=>accountId?`/projects?accountId=${encodeURIComponent(accountId)}`:'/projects',
            map:item=>{
                const value = pick(item,'projectID','projectId','ProjectID');
                const name = pick(item,'projectName','ProjectName');
                const account = pick(item,'accountID','AccountID');
                return value ? {value, label: formatLabel(name,value), accountID: account} : null;
            }
        },
        sites:{
            url:projectId=>projectId?`/sites?projectId=${encodeURIComponent(projectId)}`:'/sites',
            map:item=>{
                const value = pick(item,'siteID','siteId','SiteID');
                const name = pick(item,'siteName','SiteName');
                const project = pick(item,'projectID','ProjectID');
                return value ? {value, label: formatLabel(name,value), projectID: project} : null;
            }
        },
        clients:{
            url:siteId=>siteId?`/clients?siteId=${encodeURIComponent(siteId)}`:null,
            map:item=>{
                const value = pick(item,'clientID','clientId','ClientID');
                const name = pick(item,'clientName','ClientName');
                return value ? {value, label: formatLabel(name,value), siteID: pick(item,'siteID','SiteID')} : null;
            }
        },
        countries:{
            url:'/table/country',
            map:item=>{
                const raw = pick(item,'countryCode','CountryCode','COUNTRYCODE');
                if(!raw) return null;
                const value=String(raw).toUpperCase();
                const name = pick(item,'countryName','CountryName','COUNTRYNAME');
                const label = name ? `${name} (${value})` : value;
                return {value, label};
            }
        },
        cities:{
            url:'/table/city',
            map:item=>{
                const value = pick(item,'cityID','CityID','CITYID');
                if(!value) return null;
                const name = pick(item,'cityName','CityName','CITYNAME');
                const country = pick(item,'countryCode','CountryCode','COUNTRYCODE');
                const countryCode = country ? String(country).toUpperCase() : undefined;
                const labelParts = [name, countryCode].filter(Boolean);
                const short = String(value).split('-')[0];
                const label = labelParts.length>0 ? `${labelParts.join(' • ')} (${short})` : String(value);
                return {value, label, countryCode};
            }
        },
        software:{
            url:'/table/software',
            map:item=>{
                const value = pick(item,'softwareID','SoftwareID','SOFTWAREID');
                if(!value) return null;
                const name = pick(item,'name','Name');
                const release = pick(item,'release','Release');
                const labelParts = [name, release].filter(Boolean).join(' • ');
                const short = String(value).split('-')[0];
                const label = labelParts ? `${labelParts} (${short})` : String(value);
                return {value, label};
            }
        },
        deploymentVariants:{
            url:'/deployment-variants',
            map:item=>{
                const value = pick(item,'variantID','variantId','VariantID');
                if(!value) return null;
                const code = pick(item,'variantCode','VariantCode');
                const name = pick(item,'variantName','VariantName');
                const parts = [code, name].filter(Boolean);
                const label = parts.length>0 ? parts.join(' – ') : String(value);
                return {value, label};
            }
        }
    };

    const entityMessages = {
        Account: 'Du legst gerade einen Account an.',
        Address: 'Du legst gerade eine Adresse an.',
        AudioDevice: 'Du legst gerade ein Audiogerät an.',
        City: 'Du legst gerade eine Stadt an.',
        Country: 'Du legst gerade ein Land an.',
        DeploymentVariant: 'Du legst gerade eine Deployment-Variante an.',
        InstalledSoftware: 'Du legst gerade eine Software-Installation an.',
        PhoneIntegration: 'Du legst gerade eine Telefonintegration an.',
        Project: 'Du legst gerade ein Projekt an.',
        Radio: 'Du legst gerade ein Funkgerät an.',
        Server: 'Du legst gerade einen Server an.',
        ServiceContract: 'Du legst gerade einen Servicevertrag an.',
        Site: 'Du legst gerade einen Standort (Site) an.',
        Software: 'Du legst gerade einen Software-Datensatz an.',
        UpgradePlan: 'Du planst gerade ein Software-Upgrade.',
        WorkingPosition: 'Du legst gerade eine Arbeitsstation (Client) an.'
    };

    const entityFields = {
        Account: [
            { id: 'name', label: 'Name', component: 'input', name: 'AccountName' },
            { id: 'contact', label: 'Kontaktperson', component: 'input', name: 'ContactName', required: false },
            { id: 'email', label: 'E-Mail', component: 'input', name: 'ContactEmail', required: false },
            { id: 'phone', label: 'Telefon', component: 'input', name: 'ContactPhone', required: false },
            { id: 'vat', label: 'USt-ID', component: 'input', name: 'VatNumber', required: false },
            { id: 'country', label: 'Land', component: 'input', name: 'Country', required: false }
        ],
        Address: [
            { id: 'street', label: 'Straße', component: 'input', name: 'Street' },
            { id: 'cityID', label: 'City auswählen', component: 'asyncSelect', source: 'cities', allowManual: false, placeholder: 'City auswählen', name: 'CityID' }
        ],
        AudioDevice: [
            { id: 'client', label: 'Client auswählen', component: 'asyncSelect', source: 'clients', placeholder: 'Client auswählen', name: 'ClientID' },
            { id: 'brand', label: 'Marke', component: 'input', name: 'AudioDeviceBrand', required: false },
            { id: 'serial', label: 'Serien-Nr.', component: 'input', name: 'DeviceSerialNr', required: false },
            { id: 'fw', label: 'Firmware', component: 'input', name: 'AudioDeviceFirmware', required: false },
            { id: 'dtype', label: 'DeviceType', component: 'select', options: ['HEADSET','SPEAKER','MIC'], name: 'DeviceType' }
        ],
        City: [
            { id: 'cityId', label: 'CityID', component: 'input', dataset: { uppercase: true }, name: 'CityID' },
            { id: 'cityName', label: 'City Name', component: 'input', name: 'CityName' },
            { id: 'countryCode', label: 'Country auswählen', component: 'asyncSelect', source: 'countries', allowManual: false, placeholder: 'Land auswählen', name: 'CountryCode' }
        ],
        Country: [
            { id: 'countryCode', label: 'Country Code', component: 'input', pattern: '[A-Za-z]{2}', maxlength: 2, dataset: { uppercase: true }, autocomplete: 'off', name: 'CountryCode' },
            { id: 'countryName', label: 'Country Name', component: 'input', name: 'CountryName' }
        ],
        DeploymentVariant: [
            { id: 'variantCode', label: 'VariantCode', component: 'input', name: 'VariantCode' },
            { id: 'variantName', label: 'VariantName', component: 'input', name: 'VariantName' },
            { id: 'description', label: 'Description', component: 'input', name: 'Description', required: false },
            { id: 'active', label: 'IsActive', component: 'select', options: ['true','false'], name: 'IsActive', valueType: 'boolean' }
        ],
        InstalledSoftware: [
            { id: 'siteID', label: 'Site auswählen', component: 'asyncSelect', source: 'sites', placeholder: 'Site auswählen', allowManual: false, name: 'SiteID' },
            { id: 'softwareID', label: 'Software auswählen', component: 'asyncSelect', source: 'software', placeholder: 'Software auswählen', allowManual: false, name: 'SoftwareID' }
        ],
        PhoneIntegration: [
            { id: 'client', label: 'Client auswählen', component: 'asyncSelect', source: 'clients', placeholder: 'Client auswählen', name: 'ClientID' },
            { id: 'type', label: 'PhoneType', component: 'select', options: ['Emergency','NonEmergency','Both'], name: 'PhoneType' },
            { id: 'brand', label: 'Marke', component: 'input', name: 'PhoneBrand', required: false },
            { id: 'serial', label: 'Serien-Nr.', component: 'input', name: 'PhoneSerialNr', required: false },
            { id: 'fw', label: 'Firmware', component: 'input', name: 'PhoneFirmware', required: false }
        ],
        Project: [
            { id: 'sap', label: 'SAP-ID', component: 'input', name: 'ProjectSAPID', required: false },
            { id: 'pname', label: 'Projektname', component: 'input', name: 'ProjectName' },
            { id: 'variantId', label: 'Deployment-Variante auswählen', component: 'asyncSelect', source: 'deploymentVariants', placeholder: 'Deployment-Variante auswählen', name: 'DeploymentVariantID' },
            { id: 'bundle', label: 'Bundle-Type', component: 'input', name: 'BundleType', required: false },
            { id: 'accId', label: 'Account auswählen', component: 'asyncSelect', source: 'accounts', name: 'AccountID' },
            { id: 'addrId', label: 'Adresse auswählen', component: 'asyncSelect', source: 'addresses', allowManual: false, placeholder: 'Adresse auswählen', name: 'AddressID' }
        ],
        Radio: [
            { id: 'siteId', label: 'Site auswählen', component: 'asyncSelect', source: 'sites', name: 'SiteID' },
            { id: 'brand', label: 'Marke', component: 'input', name: 'RadioBrand', required: false },
            { id: 'serial', label: 'Serien-Nr.', component: 'input', name: 'RadioSerialNr', required: false },
            { id: 'mode', label: 'Mode', component: 'select', options: ['Analog','Digital'], name: 'Mode' },
            { id: 'standard', label: 'Digital-Standard', component: 'select', options: ['Airbus','Motorola','ESN','P25','Polycom','Teltronics'], name: 'DigitalStandard', required: false },
            { id: 'client', label: 'AssignedClientID (UUID)', component: 'asyncSelect', source: 'clients', name: 'AssignedClientID', required: false, placeholder: 'Client auswählen (optional)', dependsOn: 'siteId' }
        ],
        Server: [
            { id: 'siteId', label: 'Site auswählen', component: 'asyncSelect', source: 'sites', name: 'SiteID' },
            { id: 'name', label: 'Servername', component: 'input', name: 'ServerName' },
            { id: 'brand', label: 'Marke', component: 'input', name: 'ServerBrand', required: false },
            { id: 'serial', label: 'Serien-Nr.', component: 'input', name: 'ServerSerialNr', required: false },
            { id: 'os', label: 'Betriebssystem', component: 'input', name: 'ServerOS', required: false },
            { id: 'patch', label: 'Patch-Level', component: 'input', name: 'PatchLevel', required: false },
            { id: 'vplat', label: 'Virtual-Platform', component: 'select', options: ['BareMetal','HyperV','vSphere'], name: 'VirtualPlatform' },
            { id: 'vver', label: 'Virtual-Version', component: 'input', name: 'VirtualVersion', required: false },
            { id: 'ha', label: 'HighAvailability', component: 'select', options: ['true','false'], name: 'HighAvailability', valueType: 'boolean' }
        ],
        ServiceContract: [
            { id: 'accountID', label: 'Account auswählen', component: 'asyncSelect', source: 'accounts', placeholder: 'Account auswählen', allowManual: false, name: 'AccountID' },
            { id: 'projectID', label: 'Projekt auswählen', component: 'asyncSelect', source: 'projects', placeholder: 'Projekt auswählen', allowManual: false, dependsOn: 'accountID', name: 'ProjectID' },
            { id: 'siteID', label: 'Site auswählen', component: 'asyncSelect', source: 'sites', placeholder: 'Site auswählen', allowManual: false, dependsOn: 'projectID', emptyText: 'Keine Sites für die Auswahl', name: 'SiteID' },
            { id: 'contractNumber', label: 'Vertragsnummer', component: 'input', name: 'ContractNumber' },
            { id: 'status', label: 'Status', component: 'select', options: ['Planned','Approved','InProgress','Done','Canceled'], name: 'Status' },
            { id: 'startDate', label: 'Startdatum', component: 'input', inputType: 'date', name: 'StartDate' },
            { id: 'endDate', label: 'Enddatum', component: 'input', inputType: 'date', name: 'EndDate' }
        ],
        Site: [
            { id: 'pId', label: 'Project auswählen', component: 'asyncSelect', source: 'projects', name: 'ProjectID' },
            { id: 'name', label: 'Site-Name', component: 'input', name: 'SiteName' },
            { id: 'addrId', label: 'Adresse auswählen', component: 'asyncSelect', source: 'addresses', allowManual: false, placeholder: 'Adresse auswählen', name: 'AddressID' },
            { id: 'zone', label: 'FireZone', component: 'input', name: 'FireZone', required: false },
            { id: 'tenant', label: 'TenantCount', component: 'input', inputType: 'number', min: '0', step: '1', required: false, name: 'TenantCount' }
        ],
        Software: [
            { id: 'swName', label: 'Name', component: 'input', name: 'Name' },
            { id: 'swRelease', label: 'Release', component: 'input', name: 'Release' },
            { id: 'swRevision', label: 'Revision', component: 'input', name: 'Revision' },
            { id: 'swPhase', label: 'SupportPhase', component: 'select', options: ['Preview','Production','EoL'], name: 'SupportPhase' },
            { id: 'swLicense', label: 'License Model', component: 'input', name: 'LicenseModel', required: false },
            { id: 'swEos', label: 'End of Sales', component: 'input', inputType: 'date', name: 'EndOfSalesDate', required: false },
            { id: 'swSupportStart', label: 'Support Start', component: 'input', inputType: 'date', name: 'SupportStartDate', required: false },
            { id: 'swSupportEnd', label: 'Support End', component: 'input', inputType: 'date', name: 'SupportEndDate', required: false }
        ],
        UpgradePlan: [
            { id: 'siteID', label: 'Site auswählen', component: 'asyncSelect', source: 'sites', placeholder: 'Site auswählen', allowManual: false, name: 'SiteID' },
            { id: 'softwareID', label: 'Software auswählen', component: 'asyncSelect', source: 'software', placeholder: 'Software auswählen', allowManual: false, name: 'SoftwareID' },
            { id: 'plannedStart', label: 'Geplanter Start', component: 'input', inputType: 'date', name: 'PlannedWindowStart' },
            { id: 'plannedEnd', label: 'Geplantes Ende', component: 'input', inputType: 'date', name: 'PlannedWindowEnd' },
            { id: 'status', label: 'Status', component: 'select', options: ['Planned','Approved','InProgress','Done','Canceled'], name: 'Status' },
            { id: 'createdAt', label: 'Erstellt am', component: 'input', inputType: 'date', name: 'CreatedAt', defaultValue: () => new Date().toISOString().split('T')[0] },
            { id: 'createdBy', label: 'Erstellt von', component: 'input', name: 'CreatedBy' }
        ],
        WorkingPosition: [
            { id: 'siteId', label: 'Site auswählen', component: 'asyncSelect', source: 'sites', name: 'SiteID' },
            { id: 'name', label: 'Client-Name', component: 'input', name: 'ClientName' },
            { id: 'brand', label: 'Marke', component: 'input', name: 'ClientBrand', required: false },
            { id: 'serial', label: 'Serien-Nr.', component: 'input', name: 'ClientSerialNr', required: false },
            { id: 'os', label: 'OS', component: 'input', name: 'ClientOS', required: false },
            { id: 'patch', label: 'Patch-Level', component: 'input', name: 'PatchLevel', required: false },
            { id: 'install', label: 'InstallType', component: 'select', options: ['LOCAL','BROWSER'], name: 'InstallType' }
        ]
    };

    global.FormConfig = {
        asyncSources,
        entityMessages,
        entityFields,
        getFields(entity){
            return entityFields[entity] || [];
        },
        getMessage(entity){
            return entityMessages[entity];
        }
    };
})(window);
