package at.htlle.freq.web;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** Server zu einer Site. */
@RestController
public class ServerController {

    private final JdbcTemplate jdbc;

    public ServerController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/servers")
    public List<Map<String,Object>> findBySite(@RequestParam int siteId) {
        return jdbc.queryForList("""
                SELECT ServerID, ServerName, ServerBrand, ServerOS, VirtualPlatform
                FROM Server
                WHERE SiteID = ?
                """, siteId);
    }
}