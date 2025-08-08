package at.htlle.freq.web;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** Funkgeräte zu einer Site. */
@RestController
public class RadioController {

    private final JdbcTemplate jdbc;

    public RadioController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/radios")
    public List<Map<String,Object>> findBySite(@RequestParam int siteId) {
        return jdbc.queryForList("""
                SELECT RadioID, RadioBrand, Mode, DigitalStandard, AssignedClientID
                FROM Radio
                WHERE SiteID = ?
                """, siteId);
    }
}