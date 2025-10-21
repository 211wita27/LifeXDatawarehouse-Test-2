-- =========================================================
-- Dummy data set for LifeX Data Warehouse demo environment
-- Populates the in-memory H2 database with a minimal but
-- interlinked data set that exercises the REST endpoints and
-- Lucene indexing flows.
-- =========================================================

-- Countries
INSERT INTO Country (CountryCode, CountryName) VALUES ('AT', 'Austria');
INSERT INTO Country (CountryCode, CountryName) VALUES ('DE', 'Germany');
INSERT INTO Country (CountryCode, CountryName) VALUES ('US', 'United States');

-- Cities
INSERT INTO City (CityID, CityName, CountryCode) VALUES ('AT-VIE', 'Vienna', 'AT');
INSERT INTO City (CityID, CityName, CountryCode) VALUES ('AT-LNZ', 'Linz', 'AT');
INSERT INTO City (CityID, CityName, CountryCode) VALUES ('DE-MUC', 'Munich', 'DE');
INSERT INTO City (CityID, CityName, CountryCode) VALUES ('US-DEN', 'Denver', 'US');

-- Addresses
INSERT INTO Address (AddressID, Street, CityID) VALUES ('00000000-0000-0000-0000-000000000001', 'Innovationsring 1', 'AT-VIE');
INSERT INTO Address (AddressID, Street, CityID) VALUES ('00000000-0000-0000-0000-000000000002', 'Sonnenallee 99', 'DE-MUC');
INSERT INTO Address (AddressID, Street, CityID) VALUES ('00000000-0000-0000-0000-000000000003', 'Tech Park 200', 'US-DEN');
INSERT INTO Address (AddressID, Street, CityID) VALUES ('00000000-0000-0000-0000-000000000004', 'Donauufer 7', 'AT-LNZ');
INSERT INTO Address (AddressID, Street, CityID) VALUES ('00000000-0000-0000-0000-000000000005', 'Industriepark 12', 'AT-VIE');
INSERT INTO Address (AddressID, Street, CityID) VALUES ('00000000-0000-0000-0000-000000000006', 'Leopoldstrasse 250', 'DE-MUC');
INSERT INTO Address (AddressID, Street, CityID) VALUES ('00000000-0000-0000-0000-000000000007', 'Mountain View Blvd 18', 'US-DEN');

-- Accounts
INSERT INTO Account (AccountID, AccountName, ContactName, ContactEmail, ContactPhone, VATNumber, Country)
VALUES ('10000000-0000-0000-0000-000000000001', 'Aurum Mining GmbH', 'Katharina Leitner', 'katharina.leitner@aurum.example', '+43 1 1234 5678', 'ATU12345678', 'Austria');
INSERT INTO Account (AccountID, AccountName, ContactName, ContactEmail, ContactPhone, VATNumber, Country)
VALUES ('10000000-0000-0000-0000-000000000002', 'Skyreach Logistics AG', 'Jonas Faber', 'jonas.faber@skyreach.example', '+49 89 4567 8899', 'DE123456789', 'Germany');
INSERT INTO Account (AccountID, AccountName, ContactName, ContactEmail, ContactPhone, VATNumber, Country)
VALUES ('10000000-0000-0000-0000-000000000003', 'Frontier Response Inc.', 'Alicia Romero', 'alicia.romero@frontier.example', '+1 303 555 0180', 'US98-7654321', 'United States');

-- Deployment Variants
INSERT INTO DeploymentVariant (VariantID, VariantCode, VariantName, Description, IsActive)
VALUES ('20000000-0000-0000-0000-000000000001', 'CORE-ONPREM', 'Core On-Premise', 'Standard on-premise deployment for single site control centers.', TRUE);
INSERT INTO DeploymentVariant (VariantID, VariantCode, VariantName, Description, IsActive)
VALUES ('20000000-0000-0000-0000-000000000002', 'EDGE-CLOUD', 'Edge Cloud Hybrid', 'Distributed edge collectors with central cloud coordination.', TRUE);

-- Projects
INSERT INTO Project (ProjectID, ProjectSAPID, ProjectName, DeploymentVariantID, BundleType, CreateDateTime, StillActive, AccountID, AddressID)
VALUES ('30000000-0000-0000-0000-000000000001', 'SAP-90001', 'Vienna Emergency Upgrade', '20000000-0000-0000-0000-000000000002', 'Full Suite', DATE '2023-10-01', TRUE, '10000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001');
INSERT INTO Project (ProjectID, ProjectSAPID, ProjectName, DeploymentVariantID, BundleType, CreateDateTime, StillActive, AccountID, AddressID)
VALUES ('30000000-0000-0000-0000-000000000002', 'SAP-90002', 'Munich Command Modernization', '20000000-0000-0000-0000-000000000001', 'Premium', DATE '2023-05-15', TRUE, '10000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000002');
INSERT INTO Project (ProjectID, ProjectSAPID, ProjectName, DeploymentVariantID, BundleType, CreateDateTime, StillActive, AccountID, AddressID)
VALUES ('30000000-0000-0000-0000-000000000003', 'SAP-90003', 'Rocky Mountain Dispatch Rollout', '20000000-0000-0000-0000-000000000002', 'Edge Bundle', DATE '2022-11-20', FALSE, '10000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000003');

-- Sites
INSERT INTO Site (SiteID, SiteName, ProjectID, AddressID, FireZone, TenantCount)
VALUES ('40000000-0000-0000-0000-000000000001', 'Vienna HQ Dispatch', '30000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000005', 'Zone-A1', 12);
INSERT INTO Site (SiteID, SiteName, ProjectID, AddressID, FireZone, TenantCount)
VALUES ('40000000-0000-0000-0000-000000000002', 'Munich Data Center', '30000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000006', 'Zone-B2', 9);
INSERT INTO Site (SiteID, SiteName, ProjectID, AddressID, FireZone, TenantCount)
VALUES ('40000000-0000-0000-0000-000000000003', 'Denver Training Campus', '30000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000007', 'Zone-C1', 5);

-- Software catalogue
INSERT INTO Software (SoftwareID, Name, Release, Revision, SupportPhase, LicenseModel, EndOfSalesDate, SupportStartDate, SupportEndDate)
VALUES ('50000000-0000-0000-0000-000000000001', 'LifeX Core Suite', '5.2', '5.2.1', 'Production', 'Perpetual', DATE '2025-12-31', DATE '2023-01-01', DATE '2028-12-31');
INSERT INTO Software (SoftwareID, Name, Release, Revision, SupportPhase, LicenseModel, EndOfSalesDate, SupportStartDate, SupportEndDate)
VALUES ('50000000-0000-0000-0000-000000000002', 'Incident Bridge', '3.1', '3.1.4', 'Production', 'Subscription', DATE '2026-06-30', DATE '2023-06-01', DATE '2029-06-01');
INSERT INTO Software (SoftwareID, Name, Release, Revision, SupportPhase, LicenseModel, EndOfSalesDate, SupportStartDate, SupportEndDate)
VALUES ('50000000-0000-0000-0000-000000000003', 'Analytics Insight', '1.4', '1.4.0', 'Preview', 'Subscription', NULL, DATE '2024-01-01', NULL);

-- Servers
INSERT INTO Server (ServerID, SiteID, ServerName, ServerBrand, ServerSerialNr, ServerOS, PatchLevel, VirtualPlatform, VirtualVersion, HighAvailability)
VALUES ('60000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', 'lxf-vie-core-01', 'Dell', 'D-AT-001', 'Windows Server 2022', '2024-Q1', 'vSphere', '7.0', TRUE);
INSERT INTO Server (ServerID, SiteID, ServerName, ServerBrand, ServerSerialNr, ServerOS, PatchLevel, VirtualPlatform, VirtualVersion, HighAvailability)
VALUES ('60000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000002', 'lxf-muc-db-01', 'HPE', 'H-DE-145', 'Red Hat Enterprise Linux 9', '2023-Q4', 'HyperV', '2022', FALSE);
INSERT INTO Server (ServerID, SiteID, ServerName, ServerBrand, ServerSerialNr, ServerOS, PatchLevel, VirtualPlatform, VirtualVersion, HighAvailability)
VALUES ('60000000-0000-0000-0000-000000000003', '40000000-0000-0000-0000-000000000003', 'lxf-den-edge-01', 'Lenovo', 'L-US-083', 'Windows Server 2019', '2024-Q1', 'BareMetal', NULL, TRUE);

-- Clients
INSERT INTO Clients (ClientID, SiteID, ClientName, ClientBrand, ClientSerialNr, ClientOS, PatchLevel, InstallType)
VALUES ('70000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', 'Control Room Alpha', 'HP', 'HP-VIE-CR01', 'Windows 11 Enterprise', '23H2', 'LOCAL');
INSERT INTO Clients (ClientID, SiteID, ClientName, ClientBrand, ClientSerialNr, ClientOS, PatchLevel, InstallType)
VALUES ('70000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000001', 'Mobile Commander', 'Panasonic', 'PAN-VIE-T02', 'Android 14', '2024-02', 'BROWSER');
INSERT INTO Clients (ClientID, SiteID, ClientName, ClientBrand, ClientSerialNr, ClientOS, PatchLevel, InstallType)
VALUES ('70000000-0000-0000-0000-000000000003', '40000000-0000-0000-0000-000000000002', 'Munich Ops Floor', 'Dell', 'DEL-MUC-CL03', 'Windows 10 Enterprise', '22H2', 'LOCAL');
INSERT INTO Clients (ClientID, SiteID, ClientName, ClientBrand, ClientSerialNr, ClientOS, PatchLevel, InstallType)
VALUES ('70000000-0000-0000-0000-000000000004', '40000000-0000-0000-0000-000000000003', 'Denver Dispatch Pod', 'Getac', 'GET-DEN-CL01', 'Windows 11 Enterprise', '23H2', 'LOCAL');

-- Radios
INSERT INTO Radio (RadioID, SiteID, AssignedClientID, RadioBrand, RadioSerialNr, Mode, DigitalStandard)
VALUES ('80000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', '70000000-0000-0000-0000-000000000001', 'Motorola', 'MTR-AT-900', 'Digital', 'Motorola');
INSERT INTO Radio (RadioID, SiteID, AssignedClientID, RadioBrand, RadioSerialNr, Mode, DigitalStandard)
VALUES ('80000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000003', '70000000-0000-0000-0000-000000000004', 'Airbus', 'AIR-US-455', 'Digital', 'Airbus');

-- Audio devices
INSERT INTO AudioDevice (AudioDeviceID, ClientID, AudioDeviceBrand, DeviceSerialNr, AudioDeviceFirmware, DeviceType)
VALUES ('90000000-0000-0000-0000-000000000001', '70000000-0000-0000-0000-000000000001', 'Plantronics', 'PLN-AT-221', 'v5.4', 'HEADSET');
INSERT INTO AudioDevice (AudioDeviceID, ClientID, AudioDeviceBrand, DeviceSerialNr, AudioDeviceFirmware, DeviceType)
VALUES ('90000000-0000-0000-0000-000000000002', '70000000-0000-0000-0000-000000000002', 'Jabra', 'JAB-AT-118', 'v4.2', 'SPEAKER');
INSERT INTO AudioDevice (AudioDeviceID, ClientID, AudioDeviceBrand, DeviceSerialNr, AudioDeviceFirmware, DeviceType)
VALUES ('90000000-0000-0000-0000-000000000003', '70000000-0000-0000-0000-000000000003', 'Sennheiser', 'SEN-DE-330', 'v3.9', 'MIC');

-- Phone integrations
INSERT INTO PhoneIntegration (PhoneIntegrationID, ClientID, PhoneType, PhoneBrand, PhoneSerialNr, PhoneFirmware)
VALUES ('91000000-0000-0000-0000-000000000001', '70000000-0000-0000-0000-000000000001', 'Emergency', 'Avaya', 'AVA-AT-010', 'FW-3.2');
INSERT INTO PhoneIntegration (PhoneIntegrationID, ClientID, PhoneType, PhoneBrand, PhoneSerialNr, PhoneFirmware)
VALUES ('91000000-0000-0000-0000-000000000002', '70000000-0000-0000-0000-000000000002', 'NonEmergency', 'Cisco', 'CIS-AT-208', 'FW-12.5');
INSERT INTO PhoneIntegration (PhoneIntegrationID, ClientID, PhoneType, PhoneBrand, PhoneSerialNr, PhoneFirmware)
VALUES ('91000000-0000-0000-0000-000000000003', '70000000-0000-0000-0000-000000000004', 'Both', 'Unify', 'UNF-US-044', 'FW-2.7');

-- Installed software per site
INSERT INTO InstalledSoftware (InstalledSoftwareID, SiteID, SoftwareID)
VALUES ('92000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000001');
INSERT INTO InstalledSoftware (InstalledSoftwareID, SiteID, SoftwareID)
VALUES ('92000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000002');
INSERT INTO InstalledSoftware (InstalledSoftwareID, SiteID, SoftwareID)
VALUES ('92000000-0000-0000-0000-000000000003', '40000000-0000-0000-0000-000000000002', '50000000-0000-0000-0000-000000000001');
INSERT INTO InstalledSoftware (InstalledSoftwareID, SiteID, SoftwareID)
VALUES ('92000000-0000-0000-0000-000000000004', '40000000-0000-0000-0000-000000000003', '50000000-0000-0000-0000-000000000003');

-- Upgrade plans
INSERT INTO UpgradePlan (UpgradePlanID, SiteID, SoftwareID, PlannedWindowStart, PlannedWindowEnd, Status, CreatedAt, CreatedBy)
VALUES ('93000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000003', DATE '2024-03-01', DATE '2024-03-05', 'Planned', DATE '2024-01-12', 'pschmidt');
INSERT INTO UpgradePlan (UpgradePlanID, SiteID, SoftwareID, PlannedWindowStart, PlannedWindowEnd, Status, CreatedAt, CreatedBy)
VALUES ('93000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000002', '50000000-0000-0000-0000-000000000002', DATE '2024-02-10', DATE '2024-02-12', 'InProgress', DATE '2024-01-20', 'afischer');

-- Service contracts
INSERT INTO ServiceContract (ContractID, AccountID, ProjectID, SiteID, ContractNumber, Status, StartDate, EndDate)
VALUES ('94000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', 'SC-AT-2023-001', 'Approved', DATE '2023-09-01', DATE '2026-08-31');
INSERT INTO ServiceContract (ContractID, AccountID, ProjectID, SiteID, ContractNumber, Status, StartDate, EndDate)
VALUES ('94000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000002', 'SC-DE-2024-002', 'InProgress', DATE '2024-01-01', DATE '2025-12-31');
INSERT INTO ServiceContract (ContractID, AccountID, ProjectID, SiteID, ContractNumber, Status, StartDate, EndDate)
VALUES ('94000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000003', '40000000-0000-0000-0000-000000000003', 'SC-US-2023-005', 'Planned', DATE '2023-05-15', DATE '2024-05-14');
