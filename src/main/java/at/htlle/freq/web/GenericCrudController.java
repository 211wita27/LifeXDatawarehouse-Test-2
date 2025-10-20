package at.htlle.freq.web;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
public class GenericCrudController {

    private final NamedParameterJdbcTemplate jdbc;

    public GenericCrudController(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // -------- Whitelist gültiger Tabellen + Aliase --------
    private static final Map<String, String> TABLES;
    private static final Map<String, String> PKS;

    static {
        Map<String, String> t = new LinkedHashMap<>();
        t.put("account", "Account");
        t.put("project", "Project");
        t.put("site", "Site");
        t.put("server", "Server");
        t.put("client", "Clients");
        t.put("clients", "Clients");
        t.put("workingposition", "Clients");
        t.put("radio", "Radio");
        t.put("audiodevice", "AudioDevice");
        t.put("phoneintegration", "PhoneIntegration");
        t.put("country", "Country");
        t.put("city", "City");
        t.put("address", "Address");
        t.put("deploymentvariant", "DeploymentVariant");
        t.put("software", "Software");
        t.put("installedsoftware", "InstalledSoftware");
        t.put("upgradeplan", "UpgradePlan");
        t.put("servicecontract", "ServiceContract");
        TABLES = Collections.unmodifiableMap(t);

        Map<String, String> p = new HashMap<>();
        p.put("Account", "AccountID");
        p.put("Project", "ProjectID");
        p.put("Site", "SiteID");
        p.put("Server", "ServerID");
        p.put("Clients", "ClientID");
        p.put("Radio", "RadioID");
        p.put("AudioDevice", "AudioDeviceID");
        p.put("PhoneIntegration", "PhoneIntegrationID");
        p.put("Country", "CountryCode");
        p.put("City", "CityID");
        p.put("Address", "AddressID");
        p.put("DeploymentVariant", "VariantID");
        p.put("Software", "SoftwareID");
        p.put("InstalledSoftware", "InstalledSoftwareID");
        p.put("UpgradePlan", "UpgradePlanID");
        p.put("ServiceContract", "ContractID");
        PKS = Collections.unmodifiableMap(p);
    }

    private String normalizeTable(String name) {
        if (name == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "table missing");
        String key = name.trim().toLowerCase();
        String table = TABLES.get(key);
        if (table == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unknown table: " + name);
        }
        return table;
    }

    private static boolean pkIsString(String table) {
        return "Country".equals(table) || "City".equals(table);
    }

    // -------- READ --------

    @GetMapping("/table/{name}")
    public List<Map<String, Object>> list(@PathVariable String name,
                                          @RequestParam(name = "limit", defaultValue = "100") int limit) {
        String table = normalizeTable(name);
        limit = Math.max(1, Math.min(limit, 500));
        return jdbc.queryForList("SELECT * FROM " + table + " LIMIT " + limit, new HashMap<>());
    }

    @GetMapping("/row/{name}/{id}")
    public Map<String, Object> row(@PathVariable String name, @PathVariable String id) {
        String table = normalizeTable(name);
        String pk = PKS.get(table);
        if (pk == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "no PK known for table " + table);

        String sql = "SELECT * FROM " + table + " WHERE " + pk + " = :id";
        var params = new MapSqlParameterSource("id", id);
        List<Map<String, Object>> rows = jdbc.queryForList(sql, params);
        if (rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "not found");
        return rows.get(0);
    }

    // -------- CREATE --------
    @PostMapping("/row/{name}")
    @ResponseStatus(HttpStatus.CREATED)
    public void insert(@PathVariable String name, @RequestBody Map<String, Object> body) {
        String table = normalizeTable(name);
        if (body.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "empty body");

        var columns = String.join(", ", body.keySet());
        var values = ":" + String.join(", :", body.keySet());

        String sql = "INSERT INTO " + table + " (" + columns + ") VALUES (" + values + ")";
        jdbc.update(sql, new MapSqlParameterSource(body));
    }

    // -------- UPDATE --------
    @PutMapping("/row/{name}/{id}")
    public void update(@PathVariable String name, @PathVariable String id, @RequestBody Map<String, Object> body) {
        String table = normalizeTable(name);
        String pk = PKS.get(table);
        if (pk == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "no PK known for table " + table);

        if (body.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "empty body");

        var setClauses = new ArrayList<String>();
        for (String col : body.keySet()) {
            setClauses.add(col + " = :" + col);
        }
        String sql = "UPDATE " + table + " SET " + String.join(", ", setClauses) + " WHERE " + pk + " = :id";
        var params = new MapSqlParameterSource(body).addValue("id", id);

        int count = jdbc.update(sql, params);
        if (count == 0)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "no record updated");
    }

    // -------- DELETE --------
    @DeleteMapping("/row/{name}/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String name, @PathVariable String id) {
        String table = normalizeTable(name);
        String pk = PKS.get(table);
        if (pk == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "no PK known for table " + table);

        String sql = "DELETE FROM " + table + " WHERE " + pk + " = :id";
        int count = jdbc.update(sql, new MapSqlParameterSource("id", id));
        if (count == 0)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "no record deleted");
    }
}
