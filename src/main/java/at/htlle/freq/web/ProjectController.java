package at.htlle.freq.web;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Liefert alle Projekte zu einem Account – wird in account-details.html verwendet.
 */
@RestController
public class ProjectController {

    private final JdbcTemplate jdbc;

    public ProjectController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/projects")
    public List<Map<String, Object>> findByAccount(@RequestParam int accountId) {
        return jdbc.queryForList("""
                SELECT  ProjectID,
                        ProjectName,
                        DeploymentVariant
                FROM    Project
                WHERE   AccountID = ?
                """, accountId);
    }

    @GetMapping("/projects/all")
    public List<Map<String,Object>> findAll(){          // →  /projects/all
        return jdbc.queryForList("""
        SELECT ProjectID, ProjectName, DeploymentVariant FROM Project
        """);
    }
}