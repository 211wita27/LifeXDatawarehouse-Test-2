package at.htlle.freq.web;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** Clients (WorkingPosition) zu einer Site. */
@RestController
public class ClientController {

    private final JdbcTemplate jdbc;

    public ClientController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/clients")
    public List<Map<String,Object>> findBySite(@RequestParam int siteId) {
        return jdbc.queryForList("""
                SELECT ClientID, ClientName, ClientBrand, ClientOS
                FROM WorkingPosition
                WHERE SiteID = ?
                """, siteId);
    }
}