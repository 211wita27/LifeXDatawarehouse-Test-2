# Seed Data Plan (around 500 records)

The following target volumes are based on the schema from `src/main/resources/schema.sql` and were chosen so that a total of 500 linked records are created. The "Variation" column describes which attributes are given descriptive or varied values for readability.

| Table | Target Count | Variation for readability |
| --- | --- | --- |
| `Country` | 10 | ISO codes remain fixed, but the spelled-out country names are chosen to be diverse. |
| `City` | 30 | Combined IDs (e.g., `DE-BERLIN`) and a variety of city names per country. |
| `Address` | 60 | Different street names with numbers and city references. |
| `Account` | 30 | Distinct organization names (`Guardian Network 01`), varying contacts, phone numbers, and domains. |
| `DeploymentVariant` | 10 | Expressive codes (`URB-HA`) and descriptive names per variant. |
| `Software` | 12 | Version sequences (`2025.1`, `2025.2`), changing license and support phases. |
| `Project` | 38 | Project SAP IDs with sequence (`PX-2101`), project names like `Project Aurora 01`, boolean active flags vary. |
| `Site` | 55 | Site names referencing the city or project (`Aurora Hub Vienna`), different fire zones and tenant counts. |
| `Server` | 28 | Server names (`SRV-VIE-001`), brands, serial numbers, and OS/hypervisor combinations vary. |
| `Clients` | 40 | Operator station names (`Operator Console 014`), serial numbers, and mixed installation types (`LOCAL`/`BROWSER`). |
| `Radio` | 18 | Serial numbers with site abbreviations, different modes (`Analog`/`Digital`) and standards. |
| `AudioDevice` | 36 | Device labels (`Headset 021`), firmware versions, and device types (`HEADSET`/`SPEAKER`/`MIC`). |
| `PhoneIntegration` | 32 | Telephone types vary (`Emergency`, `NonEmergency`, `Both`), manufacturers/serial numbers change. |
| `InstalledSoftware` | 55 | Each installation links a site and software; combinations alternate between releases. |
| `UpgradePlan` | 18 | Time windows as `DATEADD` offsets, status values rotate (`Planned`, `Approved`, …). |
| `ServiceContract` | 28 | Contract numbers such as `SC-2025-030`, mixed statuses, durations as relative date values. |

**Note on UUID readability:** A dedicated sequence is maintained for each table. The last block of the UUID receives a two-digit hex prefix for the table type plus a ten-digit decimal sequence number (e.g., `05 0000000123` for the 123rd project record). This makes it easy to sort IDs in the UI while still using valid UUIDs.
