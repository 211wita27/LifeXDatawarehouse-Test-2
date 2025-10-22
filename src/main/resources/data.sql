-- =========================================================
-- Lightweight demo dataset for LifeX Data Warehouse
-- Short codes, easy-to-read labels, broader coverage
-- =========================================================

-- Countries
INSERT INTO Country (CountryCode, CountryName) VALUES
  ('AT', 'Austria'),
  ('DE', 'Germany'),
  ('CH', 'Switzerland'),
  ('US', 'USA'),
  ('UK', 'United Kingdom'),
  ('SE', 'Sweden'),
  ('FR', 'France'),
  ('ES', 'Spain');

-- Cities (compact IDs)
INSERT INTO City (CityID, CityName, CountryCode) VALUES
  ('AT1', 'Vienna', 'AT'),
  ('AT2', 'Linz', 'AT'),
  ('DE1', 'Berlin', 'DE'),
  ('DE2', 'Hamburg', 'DE'),
  ('DE3', 'Munich', 'DE'),
  ('CH1', 'Zurich', 'CH'),
  ('CH2', 'Bern', 'CH'),
  ('US1', 'Austin', 'US'),
  ('US2', 'Seattle', 'US'),
  ('US3', 'Denver', 'US'),
  ('UK1', 'London', 'UK'),
  ('UK2', 'Manchester', 'UK'),
  ('SE1', 'Stockholm', 'SE'),
  ('SE2', 'Gothenburg', 'SE'),
  ('FR1', 'Paris', 'FR'),
  ('ES1', 'Madrid', 'ES');

-- Addresses per city (three each)
INSERT INTO Address (Street, CityID) VALUES
  ('Main 1', 'AT1'), ('Ring 2', 'AT1'), ('Dock 1', 'AT2'),
  ('Gate 1', 'DE1'), ('Park 2', 'DE1'), ('Canal 1', 'DE2'),
  ('Lake 1', 'DE3'), ('Wall 2', 'DE3'), ('Bridge 1', 'CH1'),
  ('Hill 1', 'CH1'), ('Lane 2', 'CH2'), ('Field 1', 'CH2'),
  ('Oak 1', 'US1'), ('Pine 2', 'US1'), ('Elm 1', 'US2'),
  ('Bay 2', 'US2'), ('Creek 1', 'US3'), ('Mesa 2', 'US3'),
  ('King 1', 'UK1'), ('Queen 2', 'UK1'), ('Canal 2', 'UK2'),
  ('Mill 1', 'UK2'), ('Harbor 1', 'SE1'), ('Shore 2', 'SE1'),
  ('Tower 1', 'SE2'), ('Pier 2', 'SE2'), ('River 1', 'FR1'),
  ('Garden 2', 'FR1'), ('Plaza 1', 'ES1'), ('Slope 2', 'ES1'),
  ('Main 3', 'AT1'), ('Oak 3', 'US1'), ('Harbor 3', 'SE1');

-- Accounts with short numbers and contacts
INSERT INTO Account (AccountName, ContactName, ContactEmail, ContactPhone, VATNumber, Country) VALUES
  ('Alpha Ops', 'Leni Kurz', 'alpha@demo.local', '+43-1', 'AT-1', 'Austria'),
  ('Beta Rail', 'Max Dorn', 'beta@demo.local', '+49-2', 'DE-2', 'Germany'),
  ('Gamma Air', 'Nia Roth', 'gamma@demo.local', '+41-3', 'CH-3', 'Switzerland'),
  ('Delta Net', 'Oli West', 'delta@demo.local', '+1-4', 'US-4', 'USA'),
  ('Echo Grid', 'Sia Cole', 'echo@demo.local', '+44-5', 'UK-5', 'United Kingdom'),
  ('Foxtrot Care', 'Pam Iver', 'foxtrot@demo.local', '+46-6', 'SE-6', 'Sweden'),
  ('Hotel Link', 'Raj Flo', 'hotel@demo.local', '+33-7', 'FR-7', 'France'),
  ('India Move', 'Mia Luz', 'india@demo.local', '+34-8', 'ES-8', 'Spain'),
  ('Juliet City', 'Tom Aka', 'juliet@demo.local', '+43-9', 'AT-9', 'Austria'),
  ('Kilo Base', 'Una Bee', 'kilo@demo.local', '+49-3', 'DE-3', 'Germany'),
  ('Lima Site', 'Ivo Elm', 'lima@demo.local', '+41-4', 'CH-4', 'Switzerland'),
  ('Mike Field', 'Zoe Han', 'mike@demo.local', '+1-5', 'US-5', 'USA'),
  ('November Ops', 'Ian Jay', 'nov@demo.local', '+44-6', 'UK-6', 'United Kingdom'),
  ('Oscar Grid', 'Aya Kim', 'oscar@demo.local', '+46-7', 'SE-7', 'Sweden'),
  ('Papa Air', 'Leo Moe', 'papa@demo.local', '+33-8', 'FR-8', 'France'),
  ('Quebec Rail', 'Eva Neo', 'quebec@demo.local', '+34-9', 'ES-9', 'Spain');

-- Deployment variants (short codes)
INSERT INTO DeploymentVariant (VariantCode, VariantName, Description, IsActive) VALUES
  ('C1', 'Core Pack', 'Single site kit', TRUE),
  ('E1', 'Edge Pack', 'Multi site edge', TRUE),
  ('L1', 'Lite Pack', 'Compact build', TRUE),
  ('T1', 'Test Pack', 'Lab scope', FALSE),
  ('S1', 'Store Pack', 'Retail bundle', TRUE);

-- Projects across regions
INSERT INTO Project (ProjectSAPID, ProjectName, DeploymentVariantID, BundleType, CreateDateTime, StillActive, AccountID, AddressID) VALUES
  ('AT-A', 'Vienna Core', (SELECT VariantID FROM DeploymentVariant WHERE VariantCode='C1'), 'Core', DATE '2024-01-10', TRUE,
     (SELECT AccountID FROM Account WHERE AccountName='Alpha Ops'),
     (SELECT AddressID FROM Address WHERE Street='Main 1' AND CityID='AT1')),
  ('AT-B', 'Vienna Edge', (SELECT VariantID FROM DeploymentVariant WHERE VariantCode='E1'), 'Edge', DATE '2024-02-05', TRUE,
     (SELECT AccountID FROM Account WHERE AccountName='Juliet City'),
     (SELECT AddressID FROM Address WHERE Street='Ring 2' AND CityID='AT1')),
  ('DE-A', 'Berlin Hub', (SELECT VariantID FROM DeploymentVariant WHERE VariantCode='C1'), 'Core', DATE '2023-12-01', TRUE,
     (SELECT AccountID FROM Account WHERE AccountName='Beta Rail'),
     (SELECT AddressID FROM Address WHERE Street='Gate 1' AND CityID='DE1')),
  ('DE-B', 'Munich Edge', (SELECT VariantID FROM DeploymentVariant WHERE VariantCode='E1'), 'Edge', DATE '2023-11-12', TRUE,
     (SELECT AccountID FROM Account WHERE AccountName='Kilo Base'),
     (SELECT AddressID FROM Address WHERE Street='Lake 1' AND CityID='DE3')),
  ('CH-A', 'Zurich Node', (SELECT VariantID FROM DeploymentVariant WHERE VariantCode='C1'), 'Core', DATE '2023-10-20', TRUE,
     (SELECT AccountID FROM Account WHERE AccountName='Gamma Air'),
     (SELECT AddressID FROM Address WHERE Street='Bridge 1' AND CityID='CH1')),
  ('CH-B', 'Bern Lite', (SELECT VariantID FROM DeploymentVariant WHERE VariantCode='L1'), 'Lite', DATE '2024-01-15', TRUE,
     (SELECT AccountID FROM Account WHERE AccountName='Lima Site'),
     (SELECT AddressID FROM Address WHERE Street='Lane 2' AND CityID='CH2')),
  ('US-A', 'Austin Base', (SELECT VariantID FROM DeploymentVariant WHERE VariantCode='C1'), 'Core', DATE '2024-03-01', TRUE,
     (SELECT AccountID FROM Account WHERE AccountName='Delta Net'),
     (SELECT AddressID FROM Address WHERE Street='Oak 1' AND CityID='US1')),
  ('US-B', 'Seattle Link', (SELECT VariantID FROM DeploymentVariant WHERE VariantCode='E1'), 'Edge', DATE '2024-02-20', TRUE,
     (SELECT AccountID FROM Account WHERE AccountName='Mike Field'),
     (SELECT AddressID FROM Address WHERE Street='Elm 1' AND CityID='US2')),
  ('US-C', 'Denver Lab', (SELECT VariantID FROM DeploymentVariant WHERE VariantCode='T1'), 'Test', DATE '2024-01-25', FALSE,
     (SELECT AccountID FROM Account WHERE AccountName='Delta Net'),
     (SELECT AddressID FROM Address WHERE Street='Creek 1' AND CityID='US3')),
  ('UK-A', 'London Core', (SELECT VariantID FROM DeploymentVariant WHERE VariantCode='C1'), 'Core', DATE '2023-09-09', TRUE,
     (SELECT AccountID FROM Account WHERE AccountName='Echo Grid'),
     (SELECT AddressID FROM Address WHERE Street='King 1' AND CityID='UK1')),
  ('UK-B', 'Manchester Duo', (SELECT VariantID FROM DeploymentVariant WHERE VariantCode='E1'), 'Edge', DATE '2023-10-05', TRUE,
     (SELECT AccountID FROM Account WHERE AccountName='November Ops'),
     (SELECT AddressID FROM Address WHERE Street='Canal 2' AND CityID='UK2')),
  ('SE-A', 'Stockholm Line', (SELECT VariantID FROM DeploymentVariant WHERE VariantCode='S1'), 'Store', DATE '2024-03-10', TRUE,
     (SELECT AccountID FROM Account WHERE AccountName='Foxtrot Care'),
     (SELECT AddressID FROM Address WHERE Street='Harbor 1' AND CityID='SE1')),
  ('SE-B', 'Gothenburg Lite', (SELECT VariantID FROM DeploymentVariant WHERE VariantCode='L1'), 'Lite', DATE '2024-02-14', TRUE,
     (SELECT AccountID FROM Account WHERE AccountName='Oscar Grid'),
     (SELECT AddressID FROM Address WHERE Street='Tower 1' AND CityID='SE2')),
  ('FR-A', 'Paris Core', (SELECT VariantID FROM DeploymentVariant WHERE VariantCode='C1'), 'Core', DATE '2023-11-30', TRUE,
     (SELECT AccountID FROM Account WHERE AccountName='Hotel Link'),
     (SELECT AddressID FROM Address WHERE Street='River 1' AND CityID='FR1')),
  ('ES-A', 'Madrid Edge', (SELECT VariantID FROM DeploymentVariant WHERE VariantCode='E1'), 'Edge', DATE '2024-01-18', TRUE,
     (SELECT AccountID FROM Account WHERE AccountName='India Move'),
     (SELECT AddressID FROM Address WHERE Street='Plaza 1' AND CityID='ES1'));

-- Sites per project
INSERT INTO Site (SiteName, ProjectID, AddressID, FireZone, TenantCount) VALUES
  ('Vienna HQ', (SELECT ProjectID FROM Project WHERE ProjectSAPID='AT-A'),
     (SELECT AddressID FROM Address WHERE Street='Main 1' AND CityID='AT1'), 'A1', 3),
  ('Vienna Annex', (SELECT ProjectID FROM Project WHERE ProjectSAPID='AT-A'),
     (SELECT AddressID FROM Address WHERE Street='Main 3' AND CityID='AT1'), 'A2', 2),
  ('Vienna Edge', (SELECT ProjectID FROM Project WHERE ProjectSAPID='AT-B'),
     (SELECT AddressID FROM Address WHERE Street='Ring 2' AND CityID='AT1'), 'A3', 4),
  ('Linz Yard', (SELECT ProjectID FROM Project WHERE ProjectSAPID='AT-B'),
     (SELECT AddressID FROM Address WHERE Street='Dock 1' AND CityID='AT2'), 'A4', 1),
  ('Berlin Core', (SELECT ProjectID FROM Project WHERE ProjectSAPID='DE-A'),
     (SELECT AddressID FROM Address WHERE Street='Gate 1' AND CityID='DE1'), 'B1', 5),
  ('Berlin North', (SELECT ProjectID FROM Project WHERE ProjectSAPID='DE-A'),
     (SELECT AddressID FROM Address WHERE Street='Park 2' AND CityID='DE1'), 'B2', 3),
  ('Munich Base', (SELECT ProjectID FROM Project WHERE ProjectSAPID='DE-B'),
     (SELECT AddressID FROM Address WHERE Street='Lake 1' AND CityID='DE3'), 'B3', 2),
  ('Munich Yard', (SELECT ProjectID FROM Project WHERE ProjectSAPID='DE-B'),
     (SELECT AddressID FROM Address WHERE Street='Wall 2' AND CityID='DE3'), 'B4', 1),
  ('Zurich Hub', (SELECT ProjectID FROM Project WHERE ProjectSAPID='CH-A'),
     (SELECT AddressID FROM Address WHERE Street='Bridge 1' AND CityID='CH1'), 'C1', 4),
  ('Zurich Hill', (SELECT ProjectID FROM Project WHERE ProjectSAPID='CH-A'),
     (SELECT AddressID FROM Address WHERE Street='Hill 1' AND CityID='CH1'), 'C2', 1),
  ('Bern Lite', (SELECT ProjectID FROM Project WHERE ProjectSAPID='CH-B'),
     (SELECT AddressID FROM Address WHERE Street='Lane 2' AND CityID='CH2'), 'C3', 2),
  ('Bern Field', (SELECT ProjectID FROM Project WHERE ProjectSAPID='CH-B'),
     (SELECT AddressID FROM Address WHERE Street='Field 1' AND CityID='CH2'), 'C4', 1),
  ('Austin Base', (SELECT ProjectID FROM Project WHERE ProjectSAPID='US-A'),
     (SELECT AddressID FROM Address WHERE Street='Oak 1' AND CityID='US1'), 'D1', 5),
  ('Austin East', (SELECT ProjectID FROM Project WHERE ProjectSAPID='US-A'),
     (SELECT AddressID FROM Address WHERE Street='Pine 2' AND CityID='US1'), 'D2', 3),
  ('Seattle Deck', (SELECT ProjectID FROM Project WHERE ProjectSAPID='US-B'),
     (SELECT AddressID FROM Address WHERE Street='Elm 1' AND CityID='US2'), 'D3', 4),
  ('Seattle Bay', (SELECT ProjectID FROM Project WHERE ProjectSAPID='US-B'),
     (SELECT AddressID FROM Address WHERE Street='Bay 2' AND CityID='US2'), 'D4', 2),
  ('Denver Lab', (SELECT ProjectID FROM Project WHERE ProjectSAPID='US-C'),
     (SELECT AddressID FROM Address WHERE Street='Creek 1' AND CityID='US3'), 'D5', 1),
  ('Denver Store', (SELECT ProjectID FROM Project WHERE ProjectSAPID='US-C'),
     (SELECT AddressID FROM Address WHERE Street='Mesa 2' AND CityID='US3'), 'D6', 1),
  ('London Core', (SELECT ProjectID FROM Project WHERE ProjectSAPID='UK-A'),
     (SELECT AddressID FROM Address WHERE Street='King 1' AND CityID='UK1'), 'E1', 4),
  ('London Queen', (SELECT ProjectID FROM Project WHERE ProjectSAPID='UK-A'),
     (SELECT AddressID FROM Address WHERE Street='Queen 2' AND CityID='UK1'), 'E2', 2),
  ('Manchester Hub', (SELECT ProjectID FROM Project WHERE ProjectSAPID='UK-B'),
     (SELECT AddressID FROM Address WHERE Street='Canal 2' AND CityID='UK2'), 'E3', 3),
  ('Manchester Mill', (SELECT ProjectID FROM Project WHERE ProjectSAPID='UK-B'),
     (SELECT AddressID FROM Address WHERE Street='Mill 1' AND CityID='UK2'), 'E4', 1),
  ('Stockholm Store', (SELECT ProjectID FROM Project WHERE ProjectSAPID='SE-A'),
     (SELECT AddressID FROM Address WHERE Street='Harbor 1' AND CityID='SE1'), 'F1', 3),
  ('Stockholm Dock', (SELECT ProjectID FROM Project WHERE ProjectSAPID='SE-A'),
     (SELECT AddressID FROM Address WHERE Street='Shore 2' AND CityID='SE1'), 'F2', 2),
  ('Gothenburg Lite', (SELECT ProjectID FROM Project WHERE ProjectSAPID='SE-B'),
     (SELECT AddressID FROM Address WHERE Street='Tower 1' AND CityID='SE2'), 'F3', 2),
  ('Gothenburg Pier', (SELECT ProjectID FROM Project WHERE ProjectSAPID='SE-B'),
     (SELECT AddressID FROM Address WHERE Street='Pier 2' AND CityID='SE2'), 'F4', 1),
  ('Paris Core', (SELECT ProjectID FROM Project WHERE ProjectSAPID='FR-A'),
     (SELECT AddressID FROM Address WHERE Street='River 1' AND CityID='FR1'), 'G1', 4),
  ('Paris Garden', (SELECT ProjectID FROM Project WHERE ProjectSAPID='FR-A'),
     (SELECT AddressID FROM Address WHERE Street='Garden 2' AND CityID='FR1'), 'G2', 2),
  ('Madrid Edge', (SELECT ProjectID FROM Project WHERE ProjectSAPID='ES-A'),
     (SELECT AddressID FROM Address WHERE Street='Plaza 1' AND CityID='ES1'), 'H1', 3),
  ('Madrid Slope', (SELECT ProjectID FROM Project WHERE ProjectSAPID='ES-A'),
     (SELECT AddressID FROM Address WHERE Street='Slope 2' AND CityID='ES1'), 'H2', 1);

-- Software catalog
INSERT INTO Software (Name, Release, Revision, SupportPhase, LicenseModel, EndOfSalesDate, SupportStartDate, SupportEndDate) VALUES
  ('Core App', '1.0', 'a', 'Production', 'Perpetual', DATE '2027-12-31', DATE '2023-01-01', DATE '2029-12-31'),
  ('Edge App', '1.2', 'b', 'Production', 'Subscription', DATE '2027-06-30', DATE '2023-04-01', DATE '2030-06-30'),
  ('Lite App', '0.9', 'c', 'Preview', 'Subscription', NULL, DATE '2024-01-01', NULL),
  ('Store App', '2.0', 'd', 'Production', 'Perpetual', DATE '2028-03-31', DATE '2023-02-01', DATE '2030-03-31'),
  ('Voice App', '1.1', 'e', 'Production', 'Subscription', DATE '2028-09-30', DATE '2023-05-01', DATE '2031-09-30'),
  ('Test App', '0.5', 'f', 'Preview', 'Subscription', NULL, DATE '2024-02-01', NULL);

-- Servers (one per site)
INSERT INTO Server (SiteID, ServerName, ServerBrand, ServerSerialNr, ServerOS, PatchLevel, VirtualPlatform, VirtualVersion, HighAvailability) VALUES
  ((SELECT SiteID FROM Site WHERE SiteName='Vienna HQ'), 'vie-core', 'Dell', 'S-AT1', 'Win 2022', '24.1', 'vSphere', '7', TRUE),
  ((SELECT SiteID FROM Site WHERE SiteName='Vienna Annex'), 'vie-ann', 'HP', 'S-AT2', 'Win 2019', '23.4', 'HyperV', '2022', FALSE),
  ((SELECT SiteID FROM Site WHERE SiteName='Vienna Edge'), 'vie-edge', 'Lenovo', 'S-AT3', 'Ubuntu 22', '24.2', 'BareMetal', NULL, TRUE),
  ((SELECT SiteID FROM Site WHERE SiteName='Linz Yard'), 'linz-yard', 'Dell', 'S-AT4', 'Win 2022', '24.1', 'HyperV', '2022', TRUE),
  ((SELECT SiteID FROM Site WHERE SiteName='Berlin Core'), 'ber-core', 'HP', 'S-DE1', 'Win 2022', '24.1', 'vSphere', '7', TRUE),
  ((SELECT SiteID FROM Site WHERE SiteName='Berlin North'), 'ber-north', 'Fujitsu', 'S-DE2', 'Win 2019', '23.3', 'HyperV', '2022', FALSE),
  ((SELECT SiteID FROM Site WHERE SiteName='Munich Base'), 'mun-base', 'Dell', 'S-DE3', 'Win 2022', '24.1', 'BareMetal', NULL, TRUE),
  ((SELECT SiteID FROM Site WHERE SiteName='Munich Yard'), 'mun-yard', 'HP', 'S-DE4', 'Ubuntu 22', '24.1', 'BareMetal', NULL, FALSE),
  ((SELECT SiteID FROM Site WHERE SiteName='Zurich Hub'), 'zur-hub', 'Lenovo', 'S-CH1', 'Win 2022', '24.2', 'vSphere', '7', TRUE),
  ((SELECT SiteID FROM Site WHERE SiteName='Zurich Hill'), 'zur-hill', 'Dell', 'S-CH2', 'Win 2019', '23.3', 'HyperV', '2022', FALSE),
  ((SELECT SiteID FROM Site WHERE SiteName='Bern Lite'), 'bern-lite', 'HP', 'S-CH3', 'Win 2022', '24.1', 'BareMetal', NULL, TRUE),
  ((SELECT SiteID FROM Site WHERE SiteName='Bern Field'), 'bern-field', 'Dell', 'S-CH4', 'Ubuntu 22', '24.2', 'BareMetal', NULL, TRUE),
  ((SELECT SiteID FROM Site WHERE SiteName='Austin Base'), 'aus-base', 'Dell', 'S-US1', 'Win 2022', '24.1', 'vSphere', '7', TRUE),
  ((SELECT SiteID FROM Site WHERE SiteName='Austin East'), 'aus-east', 'HP', 'S-US2', 'Win 2019', '23.4', 'HyperV', '2022', TRUE),
  ((SELECT SiteID FROM Site WHERE SiteName='Seattle Deck'), 'sea-deck', 'Lenovo', 'S-US3', 'Win 2022', '24.1', 'BareMetal', NULL, TRUE),
  ((SELECT SiteID FROM Site WHERE SiteName='Seattle Bay'), 'sea-bay', 'Dell', 'S-US4', 'Ubuntu 22', '24.2', 'BareMetal', NULL, TRUE),
  ((SELECT SiteID FROM Site WHERE SiteName='Denver Lab'), 'den-lab', 'HP', 'S-US5', 'Win 2019', '23.3', 'BareMetal', NULL, FALSE),
  ((SELECT SiteID FROM Site WHERE SiteName='Denver Store'), 'den-store', 'Dell', 'S-US6', 'Ubuntu 22', '24.2', 'BareMetal', NULL, TRUE),
  ((SELECT SiteID FROM Site WHERE SiteName='London Core'), 'lon-core', 'Dell', 'S-UK1', 'Win 2022', '24.1', 'vSphere', '7', TRUE),
  ((SELECT SiteID FROM Site WHERE SiteName='London Queen'), 'lon-queen', 'HP', 'S-UK2', 'Win 2019', '23.4', 'HyperV', '2022', TRUE),
  ((SELECT SiteID FROM Site WHERE SiteName='Manchester Hub'), 'man-hub', 'Lenovo', 'S-UK3', 'Win 2022', '24.1', 'BareMetal', NULL, TRUE),
  ((SELECT SiteID FROM Site WHERE SiteName='Manchester Mill'), 'man-mill', 'Dell', 'S-UK4', 'Ubuntu 22', '24.2', 'BareMetal', NULL, TRUE),
  ((SELECT SiteID FROM Site WHERE SiteName='Stockholm Store'), 'sto-store', 'HP', 'S-SE1', 'Win 2022', '24.1', 'vSphere', '7', TRUE),
  ((SELECT SiteID FROM Site WHERE SiteName='Stockholm Dock'), 'sto-dock', 'Dell', 'S-SE2', 'Win 2019', '23.4', 'HyperV', '2022', FALSE),
  ((SELECT SiteID FROM Site WHERE SiteName='Gothenburg Lite'), 'got-lite', 'Lenovo', 'S-SE3', 'Win 2022', '24.1', 'BareMetal', NULL, TRUE),
  ((SELECT SiteID FROM Site WHERE SiteName='Gothenburg Pier'), 'got-pier', 'Dell', 'S-SE4', 'Ubuntu 22', '24.2', 'BareMetal', NULL, TRUE),
  ((SELECT SiteID FROM Site WHERE SiteName='Paris Core'), 'par-core', 'HP', 'S-FR1', 'Win 2022', '24.1', 'vSphere', '7', TRUE),
  ((SELECT SiteID FROM Site WHERE SiteName='Paris Garden'), 'par-garden', 'Dell', 'S-FR2', 'Win 2019', '23.3', 'HyperV', '2022', FALSE),
  ((SELECT SiteID FROM Site WHERE SiteName='Madrid Edge'), 'mad-edge', 'Lenovo', 'S-ES1', 'Win 2022', '24.1', 'BareMetal', NULL, TRUE),
  ((SELECT SiteID FROM Site WHERE SiteName='Madrid Slope'), 'mad-slope', 'Dell', 'S-ES2', 'Ubuntu 22', '24.2', 'BareMetal', NULL, TRUE);

-- Clients (primary workstations)
INSERT INTO Clients (SiteID, ClientName, ClientBrand, ClientSerialNr, ClientOS, PatchLevel, InstallType) VALUES
  ((SELECT SiteID FROM Site WHERE SiteName='Vienna HQ'), 'Desk 1', 'Dell', 'C-AT1', 'Win 11', '24H1', 'LOCAL'),
  ((SELECT SiteID FROM Site WHERE SiteName='Vienna HQ'), 'Desk 2', 'HP', 'C-AT2', 'Win 11', '24H1', 'BROWSER'),
  ((SELECT SiteID FROM Site WHERE SiteName='Vienna Annex'), 'Desk 1', 'Lenovo', 'C-AT3', 'Win 10', '23H2', 'LOCAL'),
  ((SELECT SiteID FROM Site WHERE SiteName='Vienna Edge'), 'Desk 1', 'Dell', 'C-AT4', 'Win 11', '24H1', 'LOCAL'),
  ((SELECT SiteID FROM Site WHERE SiteName='Vienna Edge'), 'Desk 2', 'HP', 'C-AT5', 'Win 10', '23H2', 'BROWSER'),
  ((SELECT SiteID FROM Site WHERE SiteName='Linz Yard'), 'Desk 1', 'Dell', 'C-AT6', 'Win 11', '24H1', 'LOCAL'),
  ((SELECT SiteID FROM Site WHERE SiteName='Berlin Core'), 'Desk 1', 'Dell', 'C-DE1', 'Win 11', '24H1', 'LOCAL'),
  ((SELECT SiteID FROM Site WHERE SiteName='Berlin Core'), 'Desk 2', 'HP', 'C-DE2', 'Win 10', '23H2', 'BROWSER'),
  ((SELECT SiteID FROM Site WHERE SiteName='Berlin North'), 'Desk 1', 'Lenovo', 'C-DE3', 'Win 11', '24H1', 'LOCAL'),
  ((SELECT SiteID FROM Site WHERE SiteName='Munich Base'), 'Desk 1', 'Dell', 'C-DE4', 'Win 11', '24H1', 'LOCAL'),
  ((SELECT SiteID FROM Site WHERE SiteName='Munich Yard'), 'Desk 1', 'HP', 'C-DE5', 'Win 10', '23H2', 'LOCAL');

INSERT INTO Clients (SiteID, ClientName, ClientBrand, ClientSerialNr, ClientOS, PatchLevel, InstallType) VALUES
  ((SELECT SiteID FROM Site WHERE SiteName='Zurich Hub'), 'Desk 1', 'Dell', 'C-CH1', 'Win 11', '24H1', 'LOCAL'),
  ((SELECT SiteID FROM Site WHERE SiteName='Zurich Hub'), 'Desk 2', 'HP', 'C-CH2', 'Win 10', '23H2', 'BROWSER'),
  ((SELECT SiteID FROM Site WHERE SiteName='Zurich Hill'), 'Desk 1', 'Lenovo', 'C-CH3', 'Win 11', '24H1', 'LOCAL'),
  ((SELECT SiteID FROM Site WHERE SiteName='Bern Lite'), 'Desk 1', 'Dell', 'C-CH4', 'Win 11', '24H1', 'LOCAL'),
  ((SELECT SiteID FROM Site WHERE SiteName='Bern Field'), 'Desk 1', 'HP', 'C-CH5', 'Win 10', '23H2', 'LOCAL'),
  ((SELECT SiteID FROM Site WHERE SiteName='Austin Base'), 'Desk 1', 'Dell', 'C-US1', 'Win 11', '24H1', 'LOCAL'),
  ((SELECT SiteID FROM Site WHERE SiteName='Austin Base'), 'Desk 2', 'HP', 'C-US2', 'Win 10', '23H2', 'BROWSER'),
  ((SELECT SiteID FROM Site WHERE SiteName='Austin East'), 'Desk 1', 'Lenovo', 'C-US3', 'Win 11', '24H1', 'LOCAL'),
  ((SELECT SiteID FROM Site WHERE SiteName='Seattle Deck'), 'Desk 1', 'Dell', 'C-US4', 'Win 11', '24H1', 'LOCAL'),
  ((SELECT SiteID FROM Site WHERE SiteName='Seattle Deck'), 'Desk 2', 'HP', 'C-US5', 'Win 10', '23H2', 'BROWSER'),
  ((SELECT SiteID FROM Site WHERE SiteName='Seattle Bay'), 'Desk 1', 'Lenovo', 'C-US6', 'Win 11', '24H1', 'LOCAL'),
  ((SELECT SiteID FROM Site WHERE SiteName='Denver Lab'), 'Desk 1', 'Dell', 'C-US7', 'Win 11', '24H1', 'LOCAL'),
  ((SELECT SiteID FROM Site WHERE SiteName='Denver Store'), 'Desk 1', 'HP', 'C-US8', 'Win 10', '23H2', 'LOCAL');

INSERT INTO Clients (SiteID, ClientName, ClientBrand, ClientSerialNr, ClientOS, PatchLevel, InstallType) VALUES
  ((SELECT SiteID FROM Site WHERE SiteName='London Core'), 'Desk 1', 'Dell', 'C-UK1', 'Win 11', '24H1', 'LOCAL'),
  ((SELECT SiteID FROM Site WHERE SiteName='London Core'), 'Desk 2', 'HP', 'C-UK2', 'Win 10', '23H2', 'BROWSER'),
  ((SELECT SiteID FROM Site WHERE SiteName='London Queen'), 'Desk 1', 'Lenovo', 'C-UK3', 'Win 11', '24H1', 'LOCAL'),
  ((SELECT SiteID FROM Site WHERE SiteName='Manchester Hub'), 'Desk 1', 'Dell', 'C-UK4', 'Win 11', '24H1', 'LOCAL'),
  ((SELECT SiteID FROM Site WHERE SiteName='Manchester Hub'), 'Desk 2', 'HP', 'C-UK5', 'Win 10', '23H2', 'BROWSER'),
  ((SELECT SiteID FROM Site WHERE SiteName='Manchester Mill'), 'Desk 1', 'Lenovo', 'C-UK6', 'Win 11', '24H1', 'LOCAL'),
  ((SELECT SiteID FROM Site WHERE SiteName='Stockholm Store'), 'Desk 1', 'Dell', 'C-SE1', 'Win 11', '24H1', 'LOCAL'),
  ((SELECT SiteID FROM Site WHERE SiteName='Stockholm Store'), 'Desk 2', 'HP', 'C-SE2', 'Win 10', '23H2', 'BROWSER'),
  ((SELECT SiteID FROM Site WHERE SiteName='Stockholm Dock'), 'Desk 1', 'Lenovo', 'C-SE3', 'Win 11', '24H1', 'LOCAL'),
  ((SELECT SiteID FROM Site WHERE SiteName='Gothenburg Lite'), 'Desk 1', 'Dell', 'C-SE4', 'Win 11', '24H1', 'LOCAL'),
  ((SELECT SiteID FROM Site WHERE SiteName='Gothenburg Pier'), 'Desk 1', 'HP', 'C-SE5', 'Win 10', '23H2', 'LOCAL'),
  ((SELECT SiteID FROM Site WHERE SiteName='Paris Core'), 'Desk 1', 'Dell', 'C-FR1', 'Win 11', '24H1', 'LOCAL'),
  ((SELECT SiteID FROM Site WHERE SiteName='Paris Core'), 'Desk 2', 'HP', 'C-FR2', 'Win 10', '23H2', 'BROWSER'),
  ((SELECT SiteID FROM Site WHERE SiteName='Paris Garden'), 'Desk 1', 'Lenovo', 'C-FR3', 'Win 11', '24H1', 'LOCAL'),
  ((SELECT SiteID FROM Site WHERE SiteName='Madrid Edge'), 'Desk 1', 'Dell', 'C-ES1', 'Win 11', '24H1', 'LOCAL'),
  ((SELECT SiteID FROM Site WHERE SiteName='Madrid Edge'), 'Desk 2', 'HP', 'C-ES2', 'Win 10', '23H2', 'BROWSER'),
  ((SELECT SiteID FROM Site WHERE SiteName='Madrid Slope'), 'Desk 1', 'Lenovo', 'C-ES3', 'Win 11', '24H1', 'LOCAL');

-- Radios per site
INSERT INTO Radio (SiteID, AssignedClientID, RadioBrand, RadioSerialNr, Mode, DigitalStandard) VALUES
  ((SELECT SiteID FROM Site WHERE SiteName='Vienna HQ'), (SELECT ClientID FROM Clients WHERE ClientSerialNr='C-AT1'), 'Motorola', 'R-AT1', 'Digital', 'Motorola'),
  ((SELECT SiteID FROM Site WHERE SiteName='Vienna Annex'), (SELECT ClientID FROM Clients WHERE ClientSerialNr='C-AT3'), 'Kenwood', 'R-AT2', 'Analog', NULL),
  ((SELECT SiteID FROM Site WHERE SiteName='Vienna Edge'), (SELECT ClientID FROM Clients WHERE ClientSerialNr='C-AT4'), 'Airbus', 'R-AT3', 'Digital', 'Airbus'),
  ((SELECT SiteID FROM Site WHERE SiteName='Linz Yard'), (SELECT ClientID FROM Clients WHERE ClientSerialNr='C-AT6'), 'Motorola', 'R-AT4', 'Digital', 'Motorola'),
  ((SELECT SiteID FROM Site WHERE SiteName='Berlin Core'), (SELECT ClientID FROM Clients WHERE ClientSerialNr='C-DE1'), 'Motorola', 'R-DE1', 'Digital', 'Motorola'),
  ((SELECT SiteID FROM Site WHERE SiteName='Berlin North'), (SELECT ClientID FROM Clients WHERE ClientSerialNr='C-DE3'), 'Airbus', 'R-DE2', 'Digital', 'Airbus'),
  ((SELECT SiteID FROM Site WHERE SiteName='Munich Base'), (SELECT ClientID FROM Clients WHERE ClientSerialNr='C-DE4'), 'Kenwood', 'R-DE3', 'Analog', NULL),
  ((SELECT SiteID FROM Site WHERE SiteName='Munich Yard'), (SELECT ClientID FROM Clients WHERE ClientSerialNr='C-DE5'), 'Motorola', 'R-DE4', 'Digital', 'Motorola'),
  ((SELECT SiteID FROM Site WHERE SiteName='Zurich Hub'), (SELECT ClientID FROM Clients WHERE ClientSerialNr='C-CH1'), 'Airbus', 'R-CH1', 'Digital', 'Airbus'),
  ((SELECT SiteID FROM Site WHERE SiteName='Zurich Hill'), (SELECT ClientID FROM Clients WHERE ClientSerialNr='C-CH3'), 'Kenwood', 'R-CH2', 'Analog', NULL),
  ((SELECT SiteID FROM Site WHERE SiteName='Bern Lite'), (SELECT ClientID FROM Clients WHERE ClientSerialNr='C-CH4'), 'Motorola', 'R-CH3', 'Digital', 'Motorola'),
  ((SELECT SiteID FROM Site WHERE SiteName='Bern Field'), (SELECT ClientID FROM Clients WHERE ClientSerialNr='C-CH5'), 'Airbus', 'R-CH4', 'Digital', 'Airbus');

INSERT INTO Radio (SiteID, AssignedClientID, RadioBrand, RadioSerialNr, Mode, DigitalStandard) VALUES
  ((SELECT SiteID FROM Site WHERE SiteName='Austin Base'), (SELECT ClientID FROM Clients WHERE ClientSerialNr='C-US1'), 'Motorola', 'R-US1', 'Digital', 'Motorola'),
  ((SELECT SiteID FROM Site WHERE SiteName='Austin East'), (SELECT ClientID FROM Clients WHERE ClientSerialNr='C-US3'), 'Kenwood', 'R-US2', 'Analog', NULL),
  ((SELECT SiteID FROM Site WHERE SiteName='Seattle Deck'), (SELECT ClientID FROM Clients WHERE ClientSerialNr='C-US4'), 'Motorola', 'R-US3', 'Digital', 'Motorola'),
  ((SELECT SiteID FROM Site WHERE SiteName='Seattle Bay'), (SELECT ClientID FROM Clients WHERE ClientSerialNr='C-US6'), 'Airbus', 'R-US4', 'Digital', 'Airbus'),
  ((SELECT SiteID FROM Site WHERE SiteName='Denver Lab'), (SELECT ClientID FROM Clients WHERE ClientSerialNr='C-US7'), 'Kenwood', 'R-US5', 'Analog', NULL),
  ((SELECT SiteID FROM Site WHERE SiteName='Denver Store'), (SELECT ClientID FROM Clients WHERE ClientSerialNr='C-US8'), 'Motorola', 'R-US6', 'Digital', 'Motorola'),
  ((SELECT SiteID FROM Site WHERE SiteName='London Core'), (SELECT ClientID FROM Clients WHERE ClientSerialNr='C-UK1'), 'Airbus', 'R-UK1', 'Digital', 'Airbus'),
  ((SELECT SiteID FROM Site WHERE SiteName='London Queen'), (SELECT ClientID FROM Clients WHERE ClientSerialNr='C-UK3'), 'Kenwood', 'R-UK2', 'Analog', NULL),
  ((SELECT SiteID FROM Site WHERE SiteName='Manchester Hub'), (SELECT ClientID FROM Clients WHERE ClientSerialNr='C-UK4'), 'Motorola', 'R-UK3', 'Digital', 'Motorola'),
  ((SELECT SiteID FROM Site WHERE SiteName='Manchester Mill'), (SELECT ClientID FROM Clients WHERE ClientSerialNr='C-UK6'), 'Airbus', 'R-UK4', 'Digital', 'Airbus'),
  ((SELECT SiteID FROM Site WHERE SiteName='Stockholm Store'), (SELECT ClientID FROM Clients WHERE ClientSerialNr='C-SE1'), 'Motorola', 'R-SE1', 'Digital', 'Motorola'),
  ((SELECT SiteID FROM Site WHERE SiteName='Stockholm Dock'), (SELECT ClientID FROM Clients WHERE ClientSerialNr='C-SE3'), 'Kenwood', 'R-SE2', 'Analog', NULL),
  ((SELECT SiteID FROM Site WHERE SiteName='Gothenburg Lite'), (SELECT ClientID FROM Clients WHERE ClientSerialNr='C-SE4'), 'Airbus', 'R-SE3', 'Digital', 'Airbus'),
  ((SELECT SiteID FROM Site WHERE SiteName='Gothenburg Pier'), (SELECT ClientID FROM Clients WHERE ClientSerialNr='C-SE5'), 'Motorola', 'R-SE4', 'Digital', 'Motorola'),
  ((SELECT SiteID FROM Site WHERE SiteName='Paris Core'), (SELECT ClientID FROM Clients WHERE ClientSerialNr='C-FR1'), 'Kenwood', 'R-FR1', 'Analog', NULL),
  ((SELECT SiteID FROM Site WHERE SiteName='Paris Garden'), (SELECT ClientID FROM Clients WHERE ClientSerialNr='C-FR3'), 'Motorola', 'R-FR2', 'Digital', 'Motorola'),
  ((SELECT SiteID FROM Site WHERE SiteName='Madrid Edge'), (SELECT ClientID FROM Clients WHERE ClientSerialNr='C-ES1'), 'Airbus', 'R-ES1', 'Digital', 'Airbus'),
  ((SELECT SiteID FROM Site WHERE SiteName='Madrid Slope'), (SELECT ClientID FROM Clients WHERE ClientSerialNr='C-ES3'), 'Kenwood', 'R-ES2', 'Analog', NULL);

-- Audio devices per workstation
INSERT INTO AudioDevice (ClientID, AudioDeviceBrand, DeviceSerialNr, AudioDeviceFirmware, DeviceType) VALUES
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-AT1'), 'Plantronics', 'AD-AT1', '1.0', 'HEADSET'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-AT2'), 'Jabra', 'AD-AT2', '1.0', 'SPEAKER'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-AT3'), 'Plantronics', 'AD-AT3', '1.0', 'HEADSET'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-AT4'), 'Plantronics', 'AD-AT4', '1.0', 'HEADSET'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-AT5'), 'Jabra', 'AD-AT5', '1.0', 'SPEAKER'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-AT6'), 'Plantronics', 'AD-AT6', '1.0', 'HEADSET'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-DE1'), 'Plantronics', 'AD-DE1', '1.0', 'HEADSET'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-DE2'), 'Jabra', 'AD-DE2', '1.0', 'SPEAKER'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-DE3'), 'Plantronics', 'AD-DE3', '1.0', 'HEADSET'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-DE4'), 'Plantronics', 'AD-DE4', '1.0', 'HEADSET'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-DE5'), 'Jabra', 'AD-DE5', '1.0', 'SPEAKER');

INSERT INTO AudioDevice (ClientID, AudioDeviceBrand, DeviceSerialNr, AudioDeviceFirmware, DeviceType) VALUES
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-CH1'), 'Plantronics', 'AD-CH1', '1.0', 'HEADSET'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-CH2'), 'Jabra', 'AD-CH2', '1.0', 'SPEAKER'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-CH3'), 'Plantronics', 'AD-CH3', '1.0', 'HEADSET'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-CH4'), 'Plantronics', 'AD-CH4', '1.0', 'HEADSET'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-CH5'), 'Jabra', 'AD-CH5', '1.0', 'SPEAKER'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-US1'), 'Plantronics', 'AD-US1', '1.0', 'HEADSET'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-US2'), 'Jabra', 'AD-US2', '1.0', 'SPEAKER'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-US3'), 'Plantronics', 'AD-US3', '1.0', 'HEADSET'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-US4'), 'Plantronics', 'AD-US4', '1.0', 'HEADSET'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-US5'), 'Jabra', 'AD-US5', '1.0', 'SPEAKER'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-US6'), 'Plantronics', 'AD-US6', '1.0', 'HEADSET'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-US7'), 'Plantronics', 'AD-US7', '1.0', 'HEADSET'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-US8'), 'Jabra', 'AD-US8', '1.0', 'SPEAKER');

INSERT INTO AudioDevice (ClientID, AudioDeviceBrand, DeviceSerialNr, AudioDeviceFirmware, DeviceType) VALUES
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-UK1'), 'Plantronics', 'AD-UK1', '1.0', 'HEADSET'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-UK2'), 'Jabra', 'AD-UK2', '1.0', 'SPEAKER'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-UK3'), 'Plantronics', 'AD-UK3', '1.0', 'HEADSET'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-UK4'), 'Plantronics', 'AD-UK4', '1.0', 'HEADSET'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-UK5'), 'Jabra', 'AD-UK5', '1.0', 'SPEAKER'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-UK6'), 'Plantronics', 'AD-UK6', '1.0', 'HEADSET'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-SE1'), 'Plantronics', 'AD-SE1', '1.0', 'HEADSET'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-SE2'), 'Jabra', 'AD-SE2', '1.0', 'SPEAKER'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-SE3'), 'Plantronics', 'AD-SE3', '1.0', 'HEADSET'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-SE4'), 'Plantronics', 'AD-SE4', '1.0', 'HEADSET'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-SE5'), 'Jabra', 'AD-SE5', '1.0', 'SPEAKER');

INSERT INTO AudioDevice (ClientID, AudioDeviceBrand, DeviceSerialNr, AudioDeviceFirmware, DeviceType) VALUES
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-FR1'), 'Plantronics', 'AD-FR1', '1.0', 'HEADSET'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-FR2'), 'Jabra', 'AD-FR2', '1.0', 'SPEAKER'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-FR3'), 'Plantronics', 'AD-FR3', '1.0', 'HEADSET'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-ES1'), 'Plantronics', 'AD-ES1', '1.0', 'HEADSET'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-ES2'), 'Jabra', 'AD-ES2', '1.0', 'SPEAKER'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-ES3'), 'Plantronics', 'AD-ES3', '1.0', 'HEADSET');

-- Phone integrations for primary desks
INSERT INTO PhoneIntegration (ClientID, PhoneType, PhoneBrand, PhoneSerialNr, PhoneFirmware) VALUES
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-AT1'), 'Emergency', 'Cisco', 'P-AT1', '2.0'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-AT3'), 'Both', 'Cisco', 'P-AT2', '2.0'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-AT4'), 'Emergency', 'Avaya', 'P-AT3', '2.0'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-AT6'), 'Both', 'Cisco', 'P-AT4', '2.0'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-DE1'), 'Emergency', 'Avaya', 'P-DE1', '2.0'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-DE3'), 'Both', 'Cisco', 'P-DE2', '2.0'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-DE4'), 'Emergency', 'Avaya', 'P-DE3', '2.0'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-DE5'), 'Both', 'Cisco', 'P-DE4', '2.0'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-CH1'), 'Emergency', 'Cisco', 'P-CH1', '2.0'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-CH3'), 'Both', 'Avaya', 'P-CH2', '2.0'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-CH4'), 'Emergency', 'Cisco', 'P-CH3', '2.0'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-CH5'), 'Both', 'Avaya', 'P-CH4', '2.0');

INSERT INTO PhoneIntegration (ClientID, PhoneType, PhoneBrand, PhoneSerialNr, PhoneFirmware) VALUES
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-US1'), 'Emergency', 'Cisco', 'P-US1', '2.0'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-US3'), 'Both', 'Avaya', 'P-US2', '2.0'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-US4'), 'Emergency', 'Cisco', 'P-US3', '2.0'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-US6'), 'Both', 'Avaya', 'P-US4', '2.0'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-US7'), 'Emergency', 'Cisco', 'P-US5', '2.0'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-US8'), 'Both', 'Avaya', 'P-US6', '2.0'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-UK1'), 'Emergency', 'Cisco', 'P-UK1', '2.0'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-UK3'), 'Both', 'Avaya', 'P-UK2', '2.0'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-UK4'), 'Emergency', 'Cisco', 'P-UK3', '2.0'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-UK6'), 'Both', 'Avaya', 'P-UK4', '2.0'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-SE1'), 'Emergency', 'Cisco', 'P-SE1', '2.0'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-SE3'), 'Both', 'Avaya', 'P-SE2', '2.0'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-SE4'), 'Emergency', 'Cisco', 'P-SE3', '2.0'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-SE5'), 'Both', 'Avaya', 'P-SE4', '2.0'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-FR1'), 'Emergency', 'Cisco', 'P-FR1', '2.0'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-FR3'), 'Both', 'Avaya', 'P-FR2', '2.0'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-ES1'), 'Emergency', 'Cisco', 'P-ES1', '2.0'),
  ((SELECT ClientID FROM Clients WHERE ClientSerialNr='C-ES3'), 'Both', 'Avaya', 'P-ES2', '2.0');

-- Installed software per site
INSERT INTO InstalledSoftware (SiteID, SoftwareID) VALUES
  ((SELECT SiteID FROM Site WHERE SiteName='Vienna HQ'), (SELECT SoftwareID FROM Software WHERE Name='Core App')),
  ((SELECT SiteID FROM Site WHERE SiteName='Vienna HQ'), (SELECT SoftwareID FROM Software WHERE Name='Voice App')),
  ((SELECT SiteID FROM Site WHERE SiteName='Vienna Annex'), (SELECT SoftwareID FROM Software WHERE Name='Lite App')),
  ((SELECT SiteID FROM Site WHERE SiteName='Vienna Edge'), (SELECT SoftwareID FROM Software WHERE Name='Edge App')),
  ((SELECT SiteID FROM Site WHERE SiteName='Linz Yard'), (SELECT SoftwareID FROM Software WHERE Name='Store App')),
  ((SELECT SiteID FROM Site WHERE SiteName='Berlin Core'), (SELECT SoftwareID FROM Software WHERE Name='Core App')),
  ((SELECT SiteID FROM Site WHERE SiteName='Berlin Core'), (SELECT SoftwareID FROM Software WHERE Name='Voice App')),
  ((SELECT SiteID FROM Site WHERE SiteName='Berlin North'), (SELECT SoftwareID FROM Software WHERE Name='Edge App')),
  ((SELECT SiteID FROM Site WHERE SiteName='Munich Base'), (SELECT SoftwareID FROM Software WHERE Name='Core App')),
  ((SELECT SiteID FROM Site WHERE SiteName='Munich Yard'), (SELECT SoftwareID FROM Software WHERE Name='Lite App')),
  ((SELECT SiteID FROM Site WHERE SiteName='Zurich Hub'), (SELECT SoftwareID FROM Software WHERE Name='Core App')),
  ((SELECT SiteID FROM Site WHERE SiteName='Zurich Hub'), (SELECT SoftwareID FROM Software WHERE Name='Voice App')),
  ((SELECT SiteID FROM Site WHERE SiteName='Zurich Hill'), (SELECT SoftwareID FROM Software WHERE Name='Lite App')),
  ((SELECT SiteID FROM Site WHERE SiteName='Bern Lite'), (SELECT SoftwareID FROM Software WHERE Name='Lite App')),
  ((SELECT SiteID FROM Site WHERE SiteName='Bern Field'), (SELECT SoftwareID FROM Software WHERE Name='Test App'));

INSERT INTO InstalledSoftware (SiteID, SoftwareID) VALUES
  ((SELECT SiteID FROM Site WHERE SiteName='Austin Base'), (SELECT SoftwareID FROM Software WHERE Name='Core App')),
  ((SELECT SiteID FROM Site WHERE SiteName='Austin Base'), (SELECT SoftwareID FROM Software WHERE Name='Voice App')),
  ((SELECT SiteID FROM Site WHERE SiteName='Austin East'), (SELECT SoftwareID FROM Software WHERE Name='Edge App')),
  ((SELECT SiteID FROM Site WHERE SiteName='Seattle Deck'), (SELECT SoftwareID FROM Software WHERE Name='Core App')),
  ((SELECT SiteID FROM Site WHERE SiteName='Seattle Bay'), (SELECT SoftwareID FROM Software WHERE Name='Edge App')),
  ((SELECT SiteID FROM Site WHERE SiteName='Denver Lab'), (SELECT SoftwareID FROM Software WHERE Name='Test App')),
  ((SELECT SiteID FROM Site WHERE SiteName='Denver Store'), (SELECT SoftwareID FROM Software WHERE Name='Store App')),
  ((SELECT SiteID FROM Site WHERE SiteName='London Core'), (SELECT SoftwareID FROM Software WHERE Name='Core App')),
  ((SELECT SiteID FROM Site WHERE SiteName='London Core'), (SELECT SoftwareID FROM Software WHERE Name='Voice App')),
  ((SELECT SiteID FROM Site WHERE SiteName='London Queen'), (SELECT SoftwareID FROM Software WHERE Name='Edge App')),
  ((SELECT SiteID FROM Site WHERE SiteName='Manchester Hub'), (SELECT SoftwareID FROM Software WHERE Name='Core App')),
  ((SELECT SiteID FROM Site WHERE SiteName='Manchester Mill'), (SELECT SoftwareID FROM Software WHERE Name='Lite App')),
  ((SELECT SiteID FROM Site WHERE SiteName='Stockholm Store'), (SELECT SoftwareID FROM Software WHERE Name='Store App')),
  ((SELECT SiteID FROM Site WHERE SiteName='Stockholm Dock'), (SELECT SoftwareID FROM Software WHERE Name='Edge App')),
  ((SELECT SiteID FROM Site WHERE SiteName='Gothenburg Lite'), (SELECT SoftwareID FROM Software WHERE Name='Lite App')),
  ((SELECT SiteID FROM Site WHERE SiteName='Gothenburg Pier'), (SELECT SoftwareID FROM Software WHERE Name='Store App')),
  ((SELECT SiteID FROM Site WHERE SiteName='Paris Core'), (SELECT SoftwareID FROM Software WHERE Name='Core App')),
  ((SELECT SiteID FROM Site WHERE SiteName='Paris Core'), (SELECT SoftwareID FROM Software WHERE Name='Voice App')),
  ((SELECT SiteID FROM Site WHERE SiteName='Paris Garden'), (SELECT SoftwareID FROM Software WHERE Name='Lite App')),
  ((SELECT SiteID FROM Site WHERE SiteName='Madrid Edge'), (SELECT SoftwareID FROM Software WHERE Name='Edge App')),
  ((SELECT SiteID FROM Site WHERE SiteName='Madrid Slope'), (SELECT SoftwareID FROM Software WHERE Name='Lite App'));

-- Upgrade plans
INSERT INTO UpgradePlan (SiteID, SoftwareID, PlannedWindowStart, PlannedWindowEnd, Status, CreatedAt, CreatedBy) VALUES
  ((SELECT SiteID FROM Site WHERE SiteName='Vienna HQ'), (SELECT SoftwareID FROM Software WHERE Name='Core App'), DATE '2024-05-01', DATE '2024-05-02', 'Planned', DATE '2024-03-01', 'leni'),
  ((SELECT SiteID FROM Site WHERE SiteName='Berlin Core'), (SELECT SoftwareID FROM Software WHERE Name='Voice App'), DATE '2024-04-10', DATE '2024-04-11', 'InProgress', DATE '2024-02-20', 'max'),
  ((SELECT SiteID FROM Site WHERE SiteName='Zurich Hub'), (SELECT SoftwareID FROM Software WHERE Name='Core App'), DATE '2024-03-15', DATE '2024-03-16', 'Approved', DATE '2024-02-10', 'nia'),
  ((SELECT SiteID FROM Site WHERE SiteName='Austin Base'), (SELECT SoftwareID FROM Software WHERE Name='Voice App'), DATE '2024-06-05', DATE '2024-06-06', 'Planned', DATE '2024-03-25', 'oli'),
  ((SELECT SiteID FROM Site WHERE SiteName='Seattle Deck'), (SELECT SoftwareID FROM Software WHERE Name='Edge App'), DATE '2024-05-12', DATE '2024-05-13', 'Planned', DATE '2024-03-12', 'zoe'),
  ((SELECT SiteID FROM Site WHERE SiteName='London Core'), (SELECT SoftwareID FROM Software WHERE Name='Core App'), DATE '2024-03-30', DATE '2024-03-31', 'Done', DATE '2024-02-01', 'sia');

INSERT INTO UpgradePlan (SiteID, SoftwareID, PlannedWindowStart, PlannedWindowEnd, Status, CreatedAt, CreatedBy) VALUES
  ((SELECT SiteID FROM Site WHERE SiteName='Manchester Hub'), (SELECT SoftwareID FROM Software WHERE Name='Core App'), DATE '2024-04-25', DATE '2024-04-26', 'Planned', DATE '2024-02-18', 'ian'),
  ((SELECT SiteID FROM Site WHERE SiteName='Stockholm Store'), (SELECT SoftwareID FROM Software WHERE Name='Store App'), DATE '2024-05-20', DATE '2024-05-21', 'Approved', DATE '2024-03-10', 'pam'),
  ((SELECT SiteID FROM Site WHERE SiteName='Paris Core'), (SELECT SoftwareID FROM Software WHERE Name='Core App'), DATE '2024-02-12', DATE '2024-02-13', 'Done', DATE '2024-01-05', 'raj'),
  ((SELECT SiteID FROM Site WHERE SiteName='Madrid Edge'), (SELECT SoftwareID FROM Software WHERE Name='Edge App'), DATE '2024-06-18', DATE '2024-06-19', 'Planned', DATE '2024-03-28', 'mia'),
  ((SELECT SiteID FROM Site WHERE SiteName='Denver Lab'), (SELECT SoftwareID FROM Software WHERE Name='Test App'), DATE '2024-05-08', DATE '2024-05-09', 'InProgress', DATE '2024-03-05', 'oli'),
  ((SELECT SiteID FROM Site WHERE SiteName='Gothenburg Lite'), (SELECT SoftwareID FROM Software WHERE Name='Lite App'), DATE '2024-07-10', DATE '2024-07-11', 'Planned', DATE '2024-04-02', 'aya');

-- Service contracts
INSERT INTO ServiceContract (AccountID, ProjectID, SiteID, ContractNumber, Status, StartDate, EndDate) VALUES
  ((SELECT AccountID FROM Account WHERE AccountName='Alpha Ops'), (SELECT ProjectID FROM Project WHERE ProjectSAPID='AT-A'), (SELECT SiteID FROM Site WHERE SiteName='Vienna HQ'), 'AT-C1', 'Approved', DATE '2023-01-01', DATE '2025-12-31'),
  ((SELECT AccountID FROM Account WHERE AccountName='Juliet City'), (SELECT ProjectID FROM Project WHERE ProjectSAPID='AT-B'), (SELECT SiteID FROM Site WHERE SiteName='Vienna Edge'), 'AT-C2', 'Planned', DATE '2023-04-01', DATE '2024-04-01'),
  ((SELECT AccountID FROM Account WHERE AccountName='Beta Rail'), (SELECT ProjectID FROM Project WHERE ProjectSAPID='DE-A'), (SELECT SiteID FROM Site WHERE SiteName='Berlin Core'), 'DE-C1', 'InProgress', DATE '2023-02-01', DATE '2024-12-31'),
  ((SELECT AccountID FROM Account WHERE AccountName='Kilo Base'), (SELECT ProjectID FROM Project WHERE ProjectSAPID='DE-B'), (SELECT SiteID FROM Site WHERE SiteName='Munich Base'), 'DE-C2', 'Planned', DATE '2023-03-01', DATE '2024-03-01'),
  ((SELECT AccountID FROM Account WHERE AccountName='Gamma Air'), (SELECT ProjectID FROM Project WHERE ProjectSAPID='CH-A'), (SELECT SiteID FROM Site WHERE SiteName='Zurich Hub'), 'CH-C1', 'Approved', DATE '2023-01-15', DATE '2025-01-15'),
  ((SELECT AccountID FROM Account WHERE AccountName='Lima Site'), (SELECT ProjectID FROM Project WHERE ProjectSAPID='CH-B'), (SELECT SiteID FROM Site WHERE SiteName='Bern Lite'), 'CH-C2', 'Planned', DATE '2023-05-01', DATE '2024-05-01'),
  ((SELECT AccountID FROM Account WHERE AccountName='Delta Net'), (SELECT ProjectID FROM Project WHERE ProjectSAPID='US-A'), (SELECT SiteID FROM Site WHERE SiteName='Austin Base'), 'US-C1', 'Approved', DATE '2023-06-01', DATE '2025-06-01');

INSERT INTO ServiceContract (AccountID, ProjectID, SiteID, ContractNumber, Status, StartDate, EndDate) VALUES
  ((SELECT AccountID FROM Account WHERE AccountName='Mike Field'), (SELECT ProjectID FROM Project WHERE ProjectSAPID='US-B'), (SELECT SiteID FROM Site WHERE SiteName='Seattle Deck'), 'US-C2', 'InProgress', DATE '2023-07-01', DATE '2024-07-01'),
  ((SELECT AccountID FROM Account WHERE AccountName='Mike Field'), (SELECT ProjectID FROM Project WHERE ProjectSAPID='US-B'), (SELECT SiteID FROM Site WHERE SiteName='Seattle Bay'), 'US-C3', 'Planned', DATE '2023-08-01', DATE '2024-08-01'),
  ((SELECT AccountID FROM Account WHERE AccountName='Delta Net'), (SELECT ProjectID FROM Project WHERE ProjectSAPID='US-C'), (SELECT SiteID FROM Site WHERE SiteName='Denver Lab'), 'US-C4', 'Canceled', DATE '2023-09-01', DATE '2024-09-01'),
  ((SELECT AccountID FROM Account WHERE AccountName='Echo Grid'), (SELECT ProjectID FROM Project WHERE ProjectSAPID='UK-A'), (SELECT SiteID FROM Site WHERE SiteName='London Core'), 'UK-C1', 'Approved', DATE '2023-02-01', DATE '2024-02-01'),
  ((SELECT AccountID FROM Account WHERE AccountName='November Ops'), (SELECT ProjectID FROM Project WHERE ProjectSAPID='UK-B'), (SELECT SiteID FROM Site WHERE SiteName='Manchester Hub'), 'UK-C2', 'Planned', DATE '2023-03-01', DATE '2024-03-01'),
  ((SELECT AccountID FROM Account WHERE AccountName='Foxtrot Care'), (SELECT ProjectID FROM Project WHERE ProjectSAPID='SE-A'), (SELECT SiteID FROM Site WHERE SiteName='Stockholm Store'), 'SE-C1', 'Approved', DATE '2024-01-10', DATE '2025-01-10'),
  ((SELECT AccountID FROM Account WHERE AccountName='Oscar Grid'), (SELECT ProjectID FROM Project WHERE ProjectSAPID='SE-B'), (SELECT SiteID FROM Site WHERE SiteName='Gothenburg Lite'), 'SE-C2', 'Planned', DATE '2024-02-05', DATE '2025-02-05'),
  ((SELECT AccountID FROM Account WHERE AccountName='Hotel Link'), (SELECT ProjectID FROM Project WHERE ProjectSAPID='FR-A'), (SELECT SiteID FROM Site WHERE SiteName='Paris Core'), 'FR-C1', 'Approved', DATE '2023-04-15', DATE '2025-04-15'),
  ((SELECT AccountID FROM Account WHERE AccountName='India Move'), (SELECT ProjectID FROM Project WHERE ProjectSAPID='ES-A'), (SELECT SiteID FROM Site WHERE SiteName='Madrid Edge'), 'ES-C1', 'InProgress', DATE '2023-05-20', DATE '2024-05-20');

