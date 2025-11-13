package at.htlle.freq.web;

import at.htlle.freq.application.InstalledSoftwareService;
import at.htlle.freq.application.SiteService;
import at.htlle.freq.domain.Site;
import at.htlle.freq.web.dto.SiteUpsertRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

/**
 * Fully featured CRUD controller for sites.
 *
 * <p>Uses {@link NamedParameterJdbcTemplate} for database operations.</p>
 */
@RestController
@RequestMapping("/sites")
public class SiteController {

    private final NamedParameterJdbcTemplate jdbc;
    private final SiteService siteService;
    private final InstalledSoftwareService installedSoftwareService;
    private static final Logger log = LoggerFactory.getLogger(SiteController.class);
    private static final String TABLE = "Site";

    public SiteController(NamedParameterJdbcTemplate jdbc, SiteService siteService,
                          InstalledSoftwareService installedSoftwareService) {
        this.jdbc = jdbc;
        this.siteService = siteService;
        this.installedSoftwareService = installedSoftwareService;
    }

    // READ operations: list all sites or filter by project

    /**
     * Lists sites, optionally filtered by project.
     *
     * <p>Path: {@code GET /sites}</p>
     * <p>Optional {@code projectId} query parameter narrows the result to a project.</p>
     *
     * @param projectId optional project foreign key.
     * @return 200 OK with sites as JSON.
     */
    @GetMapping
    public List<Map<String, Object>> findByProject(@RequestParam(required = false) String projectId) {
        if (projectId != null) {
            return jdbc.queryForList("""
                SELECT SiteID, SiteName, FireZone, TenantCount, AddressID, ProjectID
                FROM Site
                WHERE ProjectID = :pid
                """, new MapSqlParameterSource("pid", projectId));
        }

        return jdbc.queryForList("""
            SELECT SiteID, SiteName, FireZone, TenantCount, AddressID, ProjectID
            FROM Site
            """, new HashMap<>());
    }

    /**
     * Returns a site by ID.
     *
     * <p>Path: {@code GET /sites/{id}}</p>
     *
     * @param id site ID.
     * @return 200 OK with the field values or 404 if the ID is unknown.
     */
    @GetMapping("/{id}")
    public Map<String, Object> findById(@PathVariable String id) {
        var rows = jdbc.queryForList("""
            SELECT SiteID, SiteName, FireZone, TenantCount, AddressID, ProjectID
            FROM Site
            WHERE SiteID = :id
            """, new MapSqlParameterSource("id", id));

        if (rows.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Site not found");
        }
        return rows.get(0);
    }

    // CREATE operations

    /**
     * Creates a site.
     *
     * <p>Path: {@code POST /sites}</p>
     * <p>Request body: JSON with fields such as {@code siteName} or {@code projectID}.</p>
     *
     * @param body input payload.
     * @throws ResponseStatusException 400 if the body is empty.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@RequestBody SiteUpsertRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "empty body");
        }

        try {
            request.validateForCreate();
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }

        Site saved;
        try {
            saved = siteService.createOrUpdateSite(request.toSite());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }

        persistAssignments(saved.getSiteID(), request);
        log.info("[{}] create succeeded: id={} assignments={}", TABLE, saved.getSiteID(),
                request.normalizedAssignments().size());
    }

    // UPDATE operations

    /**
     * Updates a site.
     *
     * <p>Path: {@code PUT /sites/{id}}</p>
     * <p>Request body: JSON object with the columns to update.</p>
     *
     * @param id   site ID.
     * @param body field values.
     * @throws ResponseStatusException 400 if the body is empty, 404 if nothing was updated.
     */
    @PutMapping("/{id}")
    public void update(@PathVariable String id, @RequestBody SiteUpsertRequest request) {
        UUID siteId = parseUuid(id, "SiteID");
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "empty body");
        }

        try {
            request.validateForUpdate();
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }

        Site patch = request.toSite();
        Optional<Site> updated;
        try {
            updated = siteService.updateSite(siteId, patch);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }

        if (updated.isEmpty()) {
            log.warn("[{}] update failed: identifiers={} ", TABLE, Map.of("SiteID", siteId));
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "no site updated");
        }

        persistAssignments(siteId, request);
        log.info("[{}] update succeeded: id={} assignments={} keys={}", TABLE, siteId,
                request.normalizedAssignments().size(), summarizeUpdatedFields(patch));
    }

    // DELETE operations

    /**
     * Deletes a site.
     *
     * <p>Path: {@code DELETE /sites/{id}}</p>
     *
     * @param id site ID.
     * @throws ResponseStatusException 404 if no row was deleted.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        int count = jdbc.update("DELETE FROM Site WHERE SiteID = :id",
                new MapSqlParameterSource("id", id));

        if (count == 0) {
            log.warn("[{}] delete failed: identifiers={}", TABLE, Map.of("SiteID", id));
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "no site deleted");
        }
        log.info("[{}] delete succeeded: identifiers={}", TABLE, Map.of("SiteID", id));
    }

    private void persistAssignments(UUID siteId, SiteUpsertRequest request) {
        try {
            installedSoftwareService.replaceAssignmentsForSite(siteId, request.toInstalledSoftware(siteId));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    private UUID parseUuid(String raw, String fieldName) {
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    fieldName + " must be a valid UUID", ex);
        }
    }

    private Set<String> summarizeUpdatedFields(Site patch) {
        Set<String> fields = new LinkedHashSet<>();
        if (patch.getSiteName() != null) fields.add("SiteName");
        if (patch.getProjectID() != null) fields.add("ProjectID");
        if (patch.getAddressID() != null) fields.add("AddressID");
        if (patch.getFireZone() != null) fields.add("FireZone");
        if (patch.getTenantCount() != null) fields.add("TenantCount");
        return fields;
    }
}
