package at.htlle.freq.web;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** Sites zu einem Project. */
@RestController
public class SiteController {

    private final JdbcTemplate jdbc;

    public SiteController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/sites")
    public List<Map<String,Object>> findByProject(@RequestParam int projectId) {
        return jdbc.queryForList("""
                SELECT SiteID, SiteName, FireZone, TenantCount
                FROM Site
                WHERE ProjectID = ?
                """, projectId);
    }

    @GetMapping("/sites/all")
    public List<Map<String,Object>> findAll() {
        return jdbc.queryForList("""
                SELECT SiteID, SiteName, FireZone, TenantCount, ProjectID
                FROM Site
                """);
    }
}