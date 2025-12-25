package at.htlle.freq.infrastructure.persistence;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data access object for the generic table viewer.
 *
 * <p>Provides custom SELECT statements with joins for tables that reference
 * other entities so the returned rows already contain human-readable labels
 * (e.g., {@code SiteName}, {@code SoftwareName}) alongside the foreign key
 * identifiers. Tables without a dedicated query fall back to a plain
 * {@code SELECT *} capped by the requested {@code limit}.</p>
 */
@Repository
public class TableViewDao {

    private final NamedParameterJdbcTemplate jdbc;

    /** Map of normalized table names to custom SELECT statements including aliases. */
    private static final Map<String, String> CUSTOM_SELECTS;

    static {
        Map<String, String> selects = new HashMap<>();

        selects.put("UpgradePlan", """
            SELECT up.*, s.SiteName, sw.Name AS SoftwareName
            FROM UpgradePlan up
            LEFT JOIN Site s ON up.SiteID = s.SiteID
            LEFT JOIN Software sw ON up.SoftwareID = sw.SoftwareID
            LIMIT :limit
            """);

        selects.put("InstalledSoftware", """
            SELECT ins.*, s.SiteName, sw.Name AS SoftwareName
            FROM InstalledSoftware ins
            LEFT JOIN Site s ON ins.SiteID = s.SiteID
            LEFT JOIN Software sw ON ins.SoftwareID = sw.SoftwareID
            LIMIT :limit
            """);

        selects.put("ServiceContract", """
            SELECT sc.*, a.AccountName, p.ProjectName, s.SiteName
            FROM ServiceContract sc
            LEFT JOIN Account a ON sc.AccountID = a.AccountID
            LEFT JOIN Project p ON sc.ProjectID = p.ProjectID
            LEFT JOIN Site s ON sc.SiteID = s.SiteID
            LIMIT :limit
            """);

        selects.put("Server", """
            SELECT srv.*, s.SiteName
            FROM Server srv
            LEFT JOIN Site s ON srv.SiteID = s.SiteID
            LIMIT :limit
            """);

        selects.put("Clients", """
            SELECT c.*, s.SiteName
            FROM Clients c
            LEFT JOIN Site s ON c.SiteID = s.SiteID
            LIMIT :limit
            """);

        selects.put("Radio", """
            SELECT r.*, s.SiteName, c.ClientName AS AssignedClientName
            FROM Radio r
            LEFT JOIN Site s ON r.SiteID = s.SiteID
            LEFT JOIN Clients c ON r.AssignedClientID = c.ClientID
            LIMIT :limit
            """);

        selects.put("PhoneIntegration", """
            SELECT pi.*, c.ClientName
            FROM PhoneIntegration pi
            LEFT JOIN Clients c ON pi.ClientID = c.ClientID
            LIMIT :limit
            """);

        selects.put("AudioDevice", """
            SELECT ad.*, c.ClientName
            FROM AudioDevice ad
            LEFT JOIN Clients c ON ad.ClientID = c.ClientID
            LIMIT :limit
            """);

        selects.put("Project", """
            SELECT p.*, a.AccountName, dv.VariantName
            FROM Project p
            LEFT JOIN Account a ON p.AccountID = a.AccountID
            LEFT JOIN DeploymentVariant dv ON p.DeploymentVariantID = dv.VariantID
            LIMIT :limit
            """);

        selects.put("Site", """
            SELECT si.*, p.ProjectName
            FROM Site si
            LEFT JOIN Project p ON si.ProjectID = p.ProjectID
            LIMIT :limit
            """);

        CUSTOM_SELECTS = Collections.unmodifiableMap(selects);
    }

    public TableViewDao(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Retrieves rows for the given table name, applying custom join queries when
     * available to enrich foreign key references with their display names.
     *
     * @param table normalized table name (e.g., {@code UpgradePlan})
     * @param limit maximum number of rows to return
     * @return rows mapped to a list of column/value pairs
     */
    public List<Map<String, Object>> fetchTable(String table, int limit) {
        String sql = CUSTOM_SELECTS.getOrDefault(table, "SELECT * FROM " + table + " LIMIT :limit");
        return jdbc.queryForList(sql, new MapSqlParameterSource("limit", limit));
    }
}
