package at.htlle.freq.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

/**
 * Vollständiger CRUD-Controller für Project
 * (wird im Frontend unter /projects verwendet)
 */
@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final NamedParameterJdbcTemplate jdbc;

    public ProjectController(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ----------------------------
    // READ: Alle Projekte oder nach Account filtern
    // ----------------------------

    @GetMapping
    public List<Map<String, Object>> findByAccount(@RequestParam(required = false) String accountId) {
        if (accountId != null) {
            return jdbc.queryForList("""
                SELECT ProjectID, ProjectName, DeploymentVariantID, BundleType, AccountID, AddressID, StillActive, CreateDateTime
                FROM Project
                WHERE AccountID = :accId
                """, new MapSqlParameterSource("accId", accountId));
        }
        return jdbc.queryForList("""
            SELECT ProjectID, ProjectName, DeploymentVariantID, BundleType, AccountID, AddressID, StillActive, CreateDateTime
            FROM Project
            """, new HashMap<>());
    }

    @GetMapping("/{id}")
    public Map<String, Object> findById(@PathVariable String id) {
        var rows = jdbc.queryForList("""
            SELECT ProjectID, ProjectName, DeploymentVariantID, BundleType, AccountID, AddressID, StillActive, CreateDateTime
            FROM Project
            WHERE ProjectID = :id
            """, new MapSqlParameterSource("id", id));

        if (rows.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found");
        }
        return rows.get(0);
    }

    // ----------------------------
    // CREATE
    // ----------------------------

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@RequestBody Map<String, Object> body) {
        if (body.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "empty body");
        }

        String sql = """
            INSERT INTO Project (ProjectSAPID, ProjectName, DeploymentVariantID, BundleType, CreateDateTime, StillActive, AccountID, AddressID)
            VALUES (:projectSAPID, :projectName, :deploymentVariantID, :bundleType, CURRENT_DATE, TRUE, :accountID, :addressID)
            """;

        jdbc.update(sql, new MapSqlParameterSource(body));
    }

    // ----------------------------
    // UPDATE
    // ----------------------------

    @PutMapping("/{id}")
    public void update(@PathVariable String id, @RequestBody Map<String, Object> body) {
        if (body.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "empty body");
        }

        StringBuilder sql = new StringBuilder("UPDATE Project SET ");
        List<String> sets = new ArrayList<>();
        for (String key : body.keySet()) {
            sets.add(key + " = :" + key);
        }
        sql.append(String.join(", ", sets)).append(" WHERE ProjectID = :id");

        MapSqlParameterSource params = new MapSqlParameterSource(body).addValue("id", id);
        int updated = jdbc.update(sql.toString(), params);

        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "no project updated");
        }
    }

    // ----------------------------
    // DELETE
    // ----------------------------

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        int count = jdbc.update("DELETE FROM Project WHERE ProjectID = :id",
                new MapSqlParameterSource("id", id));

        if (count == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "no project deleted");
        }
    }
}
