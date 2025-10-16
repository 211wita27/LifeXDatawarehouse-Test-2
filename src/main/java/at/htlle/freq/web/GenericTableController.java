package at.htlle.freq.web;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

/**
 * Read-only-Zugriff auf whiteliste Tabellen – injection-sicher.
 */
@RestController
public class GenericTableController {

    private final JdbcTemplate jdbc;

    public GenericTableController(JdbcTemplate jdbc){ this.jdbc = jdbc; }

    // -------- Whitelist gültiger Tabellen + Aliase --------
    // Schlüssel = Name aus der URL (beliebige Klein-/Großschreibung), Wert = echte DB-Tabelle
    private static final Map<String,String> TABLES;
    // Primärschlüssel-Spalte je echte DB-Tabelle
    private static final Map<String,String> PKS;

    static {
        Map<String,String> t = new LinkedHashMap<>();
        t.put("account",            "Account");
        t.put("project",            "Project");
        t.put("site",               "Site");
        t.put("server",             "Server");

        // WorkingPosition wurde zu Clients → Aliase auf Clients
        t.put("client",             "Clients");
        t.put("clients",            "Clients");
        t.put("workingposition",    "Clients");

        t.put("radio",              "Radio");
        t.put("audiodevice",        "AudioDevice");
        t.put("phoneintegration",   "PhoneIntegration");

        // Zusätzlich sinnvoll, weil in Details/Parents/Children verwendet werden können:
        t.put("country",            "Country");
        t.put("city",               "City");
        t.put("address",            "Address");
        t.put("deploymentvariant",  "DeploymentVariant");
        t.put("software",           "Software");
        t.put("installedsoftware",  "InstalledSoftware");
        t.put("upgradeplan",        "UpgradePlan");
        t.put("servicecontract",    "ServiceContract");

        TABLES = Collections.unmodifiableMap(t);

        Map<String,String> p = new HashMap<>();
        p.put("Account",            "AccountID");
        p.put("Project",            "ProjectID");
        p.put("Site",               "SiteID");
        p.put("Server",             "ServerID");
        p.put("Clients",            "ClientID");           // NEU: statt WorkingPosition
        p.put("Radio",              "RadioID");
        p.put("AudioDevice",        "AudioDeviceID");
        p.put("PhoneIntegration",   "PhoneIntegrationID");
        p.put("Country",            "CountryCode");        // VARCHAR
        p.put("City",               "CityID");             // VARCHAR (natürlicher Schlüssel)
        p.put("Address",            "AddressID");
        p.put("DeploymentVariant",  "VariantID");
        p.put("Software",           "SoftwareID");
        p.put("InstalledSoftware",  "InstalledSoftwareID");
        p.put("UpgradePlan",        "UpgradePlanID");
        p.put("ServiceContract",    "ContractID");

        PKS = Collections.unmodifiableMap(p);
    }

    // -------- Helpers --------

    private String normalizeTable(String name){
        if (name == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "table missing");
        String key = name.trim().toLowerCase();
        String table = TABLES.get(key);
        if (table == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unknown table: " + name);
        }
        return table;
    }

    private static boolean looksLikeUuid(String s) {
        if (s == null) return false;
        try { UUID.fromString(s); return true; }
        catch (Exception ignored) { return false; }
    }

    /** Tabellen, deren PK KEIN UUID ist (sondern VARCHAR): Country, City */
    private static boolean pkIsString(String table) {
        return "Country".equals(table) || "City".equals(table);
    }

    // -------- Endpoints --------

    /** 100-Zeilen-Dump – für Tabellenübersicht (read-only). */
    @GetMapping("/table/{name}")
    public List<Map<String,Object>> list(@PathVariable String name,
                                         @RequestParam(name = "limit", defaultValue = "100") int limit){
        String table = normalizeTable(name);
        limit = Math.max(1, Math.min(limit, 500));
        // Tabellenname ist whitelisted → String-Konkatenation ist sicher
        String sql = "SELECT * FROM " + table + " LIMIT " + limit;
        return jdbc.queryForList(sql);
    }

    /** Einzel-Zeile für die Detail-Ansicht (read-only). */
    @GetMapping("/row/{name}/{id}")
    public Map<String,Object> row(@PathVariable String name,
                                  @PathVariable String id){
        String table = normalizeTable(name);
        String pk = PKS.get(table);
        if (pk == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "no PK known for table " + table);
        }

        String sql = "SELECT * FROM " + table + " WHERE " + pk + " = ?";

        Object param;
        if (pkIsString(table)) {
            // z.B. CountryCode / CityID sind VARCHAR
            param = id;
        } else {
            // alle anderen Tabellen haben UUID-PKs; falls keine UUID übergeben wurde,
            // versuchen wir trotzdem mit String (H2 kann i.d.R. casten), aber bevorzugt UUID
            if (looksLikeUuid(id)) {
                param = UUID.fromString(id);
            } else {
                param = id; // Fallback – gibt 0 Zeilen, falls nicht passend
            }
        }

        List<Map<String,Object>> rows = jdbc.queryForList(sql, param);
        if (rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "not found");
        return rows.get(0);
    }
}
