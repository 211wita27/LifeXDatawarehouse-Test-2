package at.htlle.freq.web;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Read-only-Zugriff auf beliebige Tabellen.
 */
@RestController
public class GenericTableController {

    private final JdbcTemplate jdbc;
    public GenericTableController(JdbcTemplate jdbc){ this.jdbc = jdbc; }

    /** 100-Zeilen-Dump – für Tabellenübersicht */
    @GetMapping("/table/{name}")
    public List<Map<String,Object>> list(@PathVariable String name){
        return jdbc.queryForList("SELECT * FROM " + name + " LIMIT 100");
    }

    /** Einzel-Zeile für die Detail-Ansicht */
    @GetMapping("/row/{name}/{id}")
    public Map<String,Object> row(@PathVariable String name,
                                  @PathVariable int    id){
        String pk;
        if ("workingposition".equalsIgnoreCase(name)) {
            pk = "ClientID";                 // abweichender PK
        } else {
            pk = name + "ID";
        }
        return jdbc.queryForMap("SELECT * FROM " + name +
                " WHERE " + pk + " = ?", id);
    }
}
