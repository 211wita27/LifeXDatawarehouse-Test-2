package at.htlle.freq.web;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Liefert *alle* Zeilen einer beliebigen Tabelle – nur read-only.
 */
@RestController
public class GenericTableController {

    private final JdbcTemplate jdbc;
    public GenericTableController(JdbcTemplate jdbc){ this.jdbc = jdbc; }

    @GetMapping("/table/{name}")
    public List<Map<String,Object>> list(@PathVariable String name){
        return jdbc.queryForList("SELECT * FROM " + name + " LIMIT 100");
    }
}