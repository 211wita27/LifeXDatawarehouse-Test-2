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

    // Whitelist gültiger Tabellen + Primärschlüssel
    private static final Map<String,String> TABLES;
    private static final Map<String,String> PKS;
    static {
        Map<String,String> t = new LinkedHashMap<>();
        t.put("account","Account");
        t.put("project","Project");
        t.put("site","Site");
        t.put("server","Server");
        t.put("workingposition","WorkingPosition");
        t.put("radio","Radio");
        t.put("audiodevice","AudioDevice");
        t.put("phoneintegration","PhoneIntegration");
        TABLES = Collections.unmodifiableMap(t);

        Map<String,String> p = new HashMap<>();
        p.put("Account",          "AccountID");
        p.put("Project",          "ProjectID");
        p.put("Site",             "SiteID");
        p.put("Server",           "ServerID");
        p.put("WorkingPosition",  "ClientID");          // Sonderfall
        p.put("Radio",            "RadioID");
        p.put("AudioDevice",      "AudioDeviceID");
        p.put("PhoneIntegration", "PhoneIntegrationID");
        PKS = Collections.unmodifiableMap(p);
    }

    private String normalizeTable(String name){
        if (name == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "table missing");
        String key = name.trim().toLowerCase();
        String table = TABLES.get(key);
        if (table == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unknown table: " + name);
        }
        return table;
    }

    /** 100-Zeilen-Dump – für Tabellenübersicht (read-only). */
    @GetMapping("/table/{name}")
    public List<Map<String,Object>> list(@PathVariable String name){
        String table = normalizeTable(name);
        // Tabellenname ist whitelisted → String-Konkatenation ist sicher
        String sql = "SELECT * FROM " + table + " LIMIT 100";
        return jdbc.queryForList(sql);
    }

    /** Einzel-Zeile für die Detail-Ansicht (read-only). */
    @GetMapping("/row/{name}/{id}")
    public Map<String,Object> row(@PathVariable String name,
                                  @PathVariable int id){
        String table = normalizeTable(name);
        String pk = PKS.get(table);
        if (pk == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "no PK known for table " + table);
        }
        String sql = "SELECT * FROM " + table + " WHERE " + pk + " = ?";
        // id wird als Prepared-Parameter gebunden
        return jdbc.queryForMap(sql, id);
    }
}