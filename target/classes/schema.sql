-- -----------------------------
-- DDL für H2 mit INT‑IDs und Inline‑FKs
-- -----------------------------

CREATE TABLE Account (
                         AccountID     INT          AUTO_INCREMENT PRIMARY KEY,
                         AccountName   VARCHAR(150) NOT NULL,
                         ContactName   VARCHAR(100) NOT NULL,
                         ContactEmail  VARCHAR(100) NOT NULL,
                         ContactPhone  VARCHAR(30)  NOT NULL,
                         VATNumber     VARCHAR(30)  NOT NULL,
                         Country       VARCHAR(50)  NOT NULL
);

CREATE TABLE City (
                      CityID        INT          AUTO_INCREMENT PRIMARY KEY,
                      CityName      VARCHAR(100) NOT NULL,
                      PostalCode    VARCHAR(20)  NOT NULL,
                      Country       VARCHAR(50)  NOT NULL
);

CREATE TABLE Address (
                         AddressID     INT          AUTO_INCREMENT PRIMARY KEY,
                         Street        VARCHAR(150) NOT NULL,
                         AddressLine2  VARCHAR(150),
                         CityID        INT          NOT NULL,
                         CONSTRAINT FK_Address_City FOREIGN KEY (CityID)
                             REFERENCES City(CityID)
);

CREATE TABLE LifeXSoftware (
                               LifeXID       INT          AUTO_INCREMENT PRIMARY KEY,
                               LifeXName     VARCHAR(100) NOT NULL,
                               Release       VARCHAR(20)  NOT NULL,
                               Revision      VARCHAR(20)  NOT NULL,
                               SupportPhase  VARCHAR(20)  NOT NULL,
                               CHECK (SupportPhase IN ('Preview','Production','EoL'))
);

CREATE TABLE AdditionalSoftware (
                                    AdditionalSoftwareID INT          AUTO_INCREMENT PRIMARY KEY,
                                    SoftwareName        VARCHAR(100) NOT NULL,
                                    Release             VARCHAR(20)  NOT NULL,
                                    Revision            VARCHAR(20)  NOT NULL,
                                    LicenseModel        VARCHAR(50)  NOT NULL
);

CREATE TABLE Project (
                         ProjectID           INT          AUTO_INCREMENT PRIMARY KEY,
                         ProjectSAPID        VARCHAR(50)  NOT NULL UNIQUE,
                         ProjectName         VARCHAR(100) NOT NULL,
                         DeploymentVariant   VARCHAR(20)  NOT NULL,
                         BundleType          VARCHAR(50),
                         CreateDateTime      DATETIME     NOT NULL,
                         StillActive         BOOLEAN      NOT NULL,
                         AccountID           INT          NOT NULL,
                         AddressID           INT          NOT NULL,
                         CHECK (DeploymentVariant IN ('MediumBundle','CustomS','CustomM','CustomL','CustomXL')),
                         CONSTRAINT FK_Project_Account FOREIGN KEY (AccountID)
                             REFERENCES Account(AccountID),
                         CONSTRAINT FK_Project_Address FOREIGN KEY (AddressID)
                             REFERENCES Address(AddressID)
);

CREATE TABLE InstalledSoftware (
                                   InstalledSoftwareID INT          AUTO_INCREMENT PRIMARY KEY,
                                   ProjectID           INT          NOT NULL,
                                   LifeXID             INT          NOT NULL,
                                   CONSTRAINT FK_InstalledSoftware_Project FOREIGN KEY (ProjectID)
                                       REFERENCES Project(ProjectID),
                                   CONSTRAINT FK_InstalledSoftware_LifeXSoftware FOREIGN KEY (LifeXID)
                                       REFERENCES LifeXSoftware(LifeXID)
);

CREATE TABLE InstalledSoftware_AdditionalSoftware (
                                                      InstalledSoftwareID INT NOT NULL,
                                                      AdditionalSoftwareID INT NOT NULL,
                                                      PRIMARY KEY (InstalledSoftwareID, AdditionalSoftwareID),
                                                      CONSTRAINT FK_ISAS_InstalledSoftware FOREIGN KEY (InstalledSoftwareID)
                                                          REFERENCES InstalledSoftware(InstalledSoftwareID),
                                                      CONSTRAINT FK_ISAS_AdditionalSoftware FOREIGN KEY (AdditionalSoftwareID)
                                                          REFERENCES AdditionalSoftware(AdditionalSoftwareID)
);

CREATE TABLE Site (
                      SiteID          INT          AUTO_INCREMENT PRIMARY KEY,
                      SiteName        VARCHAR(100) NOT NULL,
                      ProjectID       INT          NOT NULL,
                      AddressID       INT          NOT NULL,
                      HighAvailability BOOLEAN     NOT NULL,
                      RedundantServers INT         NOT NULL,
                      FireZone        VARCHAR(50)  NOT NULL,
                      TenantCount     INT          NOT NULL,
                      CONSTRAINT FK_Site_Project FOREIGN KEY (ProjectID)
                          REFERENCES Project(ProjectID),
                      CONSTRAINT FK_Site_Address FOREIGN KEY (AddressID)
                          REFERENCES Address(AddressID)
);

CREATE TABLE Server (
                        ServerID        INT          AUTO_INCREMENT PRIMARY KEY,
                        SiteID          INT          NOT NULL,
                        ServerName      VARCHAR(100) NOT NULL,
                        ServerBrand     VARCHAR(50)  NOT NULL,
                        ServerSerialNr  VARCHAR(100) NOT NULL,
                        ServerOS        VARCHAR(100) NOT NULL,
                        PatchLevel      VARCHAR(50)  NOT NULL,
                        VirtualPlatform VARCHAR(20)  NOT NULL,
                        VirtualVersion  VARCHAR(50),
                        NISCubeVersion  VARCHAR(30),
                        CHECK (VirtualPlatform IN ('BareMetal','HyperV','vSphere')),
                        CONSTRAINT FK_Server_Site FOREIGN KEY (SiteID)
                            REFERENCES Site(SiteID)
);

CREATE TABLE WorkingPosition (
                                 ClientID        INT          AUTO_INCREMENT PRIMARY KEY,
                                 SiteID          INT          NOT NULL,
                                 ClientName      VARCHAR(100) NOT NULL,
                                 ClientBrand     VARCHAR(50)  NOT NULL,
                                 ClientSerialNr  VARCHAR(100) NOT NULL,
                                 ClientOS        VARCHAR(100) NOT NULL,
                                 PatchLevel      VARCHAR(50)  NOT NULL,
                                 LocallyInstalled BOOLEAN     NOT NULL,
                                 CONSTRAINT FK_WorkingPosition_Site FOREIGN KEY (SiteID)
                                     REFERENCES Site(SiteID)
);

CREATE TABLE Radio (
                       RadioID             INT          AUTO_INCREMENT PRIMARY KEY,
                       SiteID              INT          NOT NULL,
                       AssignedClientID    INT,
                       RadioBrand          VARCHAR(50)  NOT NULL,
                       RadioSerialNr       VARCHAR(100) NOT NULL,
                       Mode                VARCHAR(10)  NOT NULL,
                       DigitalStandard     VARCHAR(20),
                       CHECK (Mode IN ('Analog','Digital')),
                       CHECK (DigitalStandard IS NULL OR DigitalStandard IN ('Airbus','Motorola','ESN','P25','Polycom','Teltronics')),
                       CONSTRAINT FK_Radio_Site FOREIGN KEY (SiteID)
                           REFERENCES Site(SiteID),
                       CONSTRAINT FK_Radio_WorkingPosition FOREIGN KEY (AssignedClientID)
                           REFERENCES WorkingPosition(ClientID)
);

CREATE TABLE AudioDevice (
                             AudioDeviceID       INT          AUTO_INCREMENT PRIMARY KEY,
                             ClientID            INT          NOT NULL,
                             AudioDeviceBrand    VARCHAR(50)  NOT NULL,
                             DeviceSerialNr      VARCHAR(100) NOT NULL,
                             AudioDeviceFirmware VARCHAR(50)  NOT NULL,
                             Direction           VARCHAR(6)   NOT NULL,
                             CHECK (Direction IN ('IN','OUT','INOUT')),
                             CONSTRAINT FK_AudioDevice_WorkingPosition FOREIGN KEY (ClientID)
                                 REFERENCES WorkingPosition(ClientID)
);

CREATE TABLE PhoneIntegration (
                                  PhoneIntegrationID  INT          AUTO_INCREMENT PRIMARY KEY,
                                  ClientID            INT          NOT NULL,
                                  PhoneType           VARCHAR(10)  NOT NULL,
                                  PhoneBrand          VARCHAR(50)  NOT NULL,
                                  PhoneSerialNr       VARCHAR(100) NOT NULL,
                                  PhoneFirmware       VARCHAR(50)  NOT NULL,
                                  CHECK (PhoneType IN ('Emergency','NonEmergency','Both')),
                                  CONSTRAINT FK_PhoneIntegration_WorkingPosition FOREIGN KEY (ClientID)
                                      REFERENCES WorkingPosition(ClientID)
);
