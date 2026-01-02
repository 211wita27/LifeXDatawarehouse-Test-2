package at.htlle.freq.infrastructure.persistence;

import at.htlle.freq.domain.ProjectSiteAssignmentRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class JdbcProjectSiteAssignmentRepository implements ProjectSiteAssignmentRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcProjectSiteAssignmentRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<UUID> findProjectIdsBySite(UUID siteId) {
        String sql = "SELECT ProjectID FROM ProjectSite WHERE SiteID = :sid ORDER BY ProjectID";
        return jdbc.query(sql, new MapSqlParameterSource("sid", siteId), (rs, n) -> rs.getObject("ProjectID", UUID.class));
    }

    @Override
    public List<UUID> findSiteIdsByProject(UUID projectId) {
        String sql = "SELECT SiteID FROM ProjectSite WHERE ProjectID = :pid ORDER BY SiteID";
        return jdbc.query(sql, new MapSqlParameterSource("pid", projectId), (rs, n) -> rs.getObject("SiteID", UUID.class));
    }

    @Override
    public void replaceProjectsForSite(UUID siteId, Collection<UUID> projectIds) {
        MapSqlParameterSource params = new MapSqlParameterSource("sid", siteId);
        jdbc.update("DELETE FROM ProjectSite WHERE SiteID = :sid", params);
        bulkInsert(projectIds, params, true);
    }

    @Override
    public void replaceSitesForProject(UUID projectId, Collection<UUID> siteIds) {
        MapSqlParameterSource params = new MapSqlParameterSource("pid", projectId);
        jdbc.update("DELETE FROM ProjectSite WHERE ProjectID = :pid", params);
        bulkInsert(siteIds, params, false);
    }

    private void bulkInsert(Collection<UUID> ids, MapSqlParameterSource baseParams, boolean idsRepresentProjects) {
        List<UUID> distinct = ids == null ? List.of() : ids.stream().filter(java.util.Objects::nonNull).distinct().toList();
        if (distinct.isEmpty()) {
            return;
        }

        List<MapSqlParameterSource> batch = distinct.stream()
                .map(id -> new MapSqlParameterSource()
                        .addValues(baseParams.getValues())
                        .addValue(idsRepresentProjects ? "pid" : "sid", id))
                .collect(Collectors.toList());

        String sql = idsRepresentProjects
                ? "INSERT INTO ProjectSite (ProjectID, SiteID) VALUES (:pid, :sid)"
                : "INSERT INTO ProjectSite (ProjectID, SiteID) VALUES (:pid, :sid)";

        jdbc.batchUpdate(sql, batch.toArray(MapSqlParameterSource[]::new));
    }
}
