# LifeX Data Warehouse

---

## 📑 Projektüberblick

LifeX Data Warehouse ist eine schlanke, aber funktionsreiche Demo-Applikation, die im Rahmen einer Diplomarbeit an der HTL Leoben (Abteilung Informationstechnik) entsteht.  
Sie vereint **ETL-ähnliche Datenhaltung**, **Volltextsuche via Lucene**, ein **leichtgewichtiges Web-UI** und eine **REST-API** in einem einzigen Spring-Boot-Projekt.

> **Mission Statement**  
> „Statische Stammdaten (Account → Project → Site …) sollen schnell erfasst, durchsucht und exportiert werden können – ohne schwergewichtige BI-Tools.“

---

## ✨ Haupt-Features

- **Datenmodell** – relationale H2-In-Memory-DB (Account, Project, Site, Server …)
- **API** – CRUD-REST-Controller je Entität + generischer Read-Only-Controller
- **Indexing** – Apache Lucene 8 (Full-Reindex alle 60 s + inkrementeller Camel-Sync)
- **Suche** – globale Lucene-Query-Syntax im Dashboard und via `/search?q=`
- **UI** – rein statisches HTML / CSS / JS (kein Build-Tool erforderlich)
- **Automation** – Apache Camel 4 Timer-Routes (Sync, Full-Reindex, Einzel-Index)
- **Dev-Ergonomie** – Spring Boot DevTools, LiveReload, H2-Console, Lombok

---

## 🏗️ Architektur-Überblick

```text
┌──────────────────────────────┐     Timer          ┌─────────────────────────┐
│             UI               │  (Camel 4)         │      Lucene Index       │
│  static/ (HTML + JS + CSS)   │ ────────────►      │   · account docs        │
└────────────┬─────────────────┘                    │   · project docs        │
             │  REST (JSON)                         └────────────┬────────────┘
┌────────────▼─────────────────┐  Spring Boot 3 (Java 22)        │ search()
│          Web Layer           │                                 │
│  AccountController …         │ ◄───────────────────────────────┘
└────────────┬─────────────────┘        JDBC
             │                                 ┌─────────────────────────┐
┌────────────▼─────────────────┐            ┌─►│  H2 Database (memory)   │
│       Service Layer          │            │  └─────────────────────────┘
│  AccountService …            │            │
└────────────┬─────────────────┘            │
             │ Repository (NamedParamJdbc)  │
┌────────────▼─────────────────┐            │
│        Domain Model          │            │
│  POJOs + Lombok DTOs         │ ◄──────────┘
└──────────────────────────────┘
```

---

## 🧰 Tech-Stack

- Java 22 (22.0.1)
- Spring Boot 3.4.6
- H2 Database 2.3.x
- Apache Lucene 8.11.4
- Apache Camel 4.4.1
- Maven 3.9+
- Lombok & Spring DevTools

---

## 🚀 Build & Run

```bash
# Repository klonen
git clone https://github.com/<user>/LifeXDatawarehouse.git
cd LifeXDatawarehouse

# Start im Dev-Modus
mvn spring-boot:run
```

**Öffnen im Browser:**

- http://localhost:8080
- Hot-Reload via DevTools
- H2-Console: `/h2-console` (JDBC-URL: `jdbc:h2:mem:testdb`)

---

## 🌐 REST-API (Schnellreferenz)

- `GET  /accounts` – alle Accounts
- `GET  /accounts/{id}` – einzelner Account
- `POST /accounts` – neuen Account anlegen (JSON-Body)
- `GET  /search?q=…` – globale Lucene-Suche
- `GET  /table/{name}` – 100-Zeilen-Dump einer Tabelle
- `GET  /row/{name}/{id}` – Einzel Zeile (Detail-View)

Weitere Endpunkte für `Project`, `Site`, `Server` usw. analog.

---

## 🖥️ Frontend-Seiten

- `index.html` – Dashboard
    - Suchleiste (Lucene)
    - Shortcut-Buttons (editierbar)
    - Tabellen-Explorer
- `create.html` – Wizard zum Anlegen neuer Datensätze
- `details.html` – generische Key/Value-Detailseite

**Alle Assets:**  
Liegen unter `src/main/resources/static/` – kein Frontend-Build nötig.

---

## 🔍 Lucene Quick Ref

```text
tech*                       # Wildcard  
"green valley"              # Phrase  
+foo -bar                   # Muss / Nicht  
country:germany             # Feldsuche  
(type:project AND CustomL) OR CustomXL
```

**Indexierte Felder (Beispiele):**

- Account → `txt` (Name), `country`
- Project → `txt` (Name), `variant`
- Site    → `txt` (Name), `fireZone`
- Server  → `txt` (Name), `os`

```text
erDiagram
    Account ||--o{ Project           : owns
    Project ||--o{ Site              : hosts
    Site    ||--o{ Server            : contains
    Site    ||--o{ WorkingPosition   : "WP"
    WorkingPosition ||--|{ AudioDevice      : has
    WorkingPosition ||--|{ PhoneIntegration : phones
```

*(Die vollständige SQL-Definition findest du in `schema.sql`.)*

---

## 🛡️ Qualität & CI

- (geplant) Unit-Tests mit JUnit 5
- Beispiel-GitHub Actions Workflow (`mvn test` + Docker build)
- Checkstyle und SpotBugs (TODO)

---

## 🚧 Roadmap

- ✔️ Lucene-Index + globale Suche
- ✔️ Shortcut-UI (editierbar)
- ✔️ Create-Wizard
- ☐ CSV / Excel-Export per REST
- ☐ Benutzer-Auth (Spring Security + JWT)
- ☐ Docker-Compose (PostgreSQL + OpenSearch)

---

## 👥 Mitwirkende

- Mario Ziegerhofer – Entwickler
- Marcel Papic – Entwickler
- Alexander Schüller – Team-Lead

---

## 📄 Lizenz

Dieses Projekt steht unter der MIT-Lizenz – siehe `LICENSE`.

---

© 2025 Mario Ziegerhofer • HTL Leoben Informationstechnik • Alle Angaben ohne Gewähr