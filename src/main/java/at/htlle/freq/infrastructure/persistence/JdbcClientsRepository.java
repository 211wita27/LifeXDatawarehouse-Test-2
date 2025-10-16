package at.htlle.freq.infrastructure.persistence;

import at.htlle.freq.domain.Clients;
import at.htlle.freq.domain.ClientsRepository;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class JdbcClientsRepository implements ClientsRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcClientsRepository(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }

    private final RowMapper<Clients> mapper = (rs, n) -> new Clients(
            rs.getObject("ClientID", UUID.class),
            rs.getObject("SiteID", UUID.class),
            rs.getString("ClientName"),
            rs.getString("ClientBrand"),
            rs.getString("ClientSerialNr"),
            rs.getString("ClientOS"),
            rs.getString("PatchLevel"),
            rs.getString("InstallType")
    );

    @Override
    public Optional<Clients> findById(UUID id) {
        String sql = """
            SELECT ClientID, SiteID, ClientName, ClientBrand, ClientSerialNr, ClientOS, PatchLevel, InstallType
            FROM Clients WHERE ClientID = :id
            """;
        try { return Optional.ofNullable(jdbc.queryForObject(sql, new MapSqlParameterSource("id", id), mapper)); }
        catch (Exception e) { return Optional.empty(); }
    }

    @Override
    public List<Clients> findBySite(UUID siteId) {
        String sql = """
            SELECT ClientID, SiteID, ClientName, ClientBrand, ClientSerialNr, ClientOS, PatchLevel, InstallType
            FROM Clients WHERE SiteID = :sid
            """;
        return jdbc.query(sql, new MapSqlParameterSource("sid", siteId), mapper);
    }

    @Override
    public List<Clients> findAll() {
        return jdbc.query("""
            SELECT ClientID, SiteID, ClientName, ClientBrand, ClientSerialNr, ClientOS, PatchLevel, InstallType
            FROM Clients
            """, mapper);
    }

    @Override
    public void save(Clients c) {
        boolean isNew = c.getClientID() == null;
        if (isNew) {
            String sql = """
                INSERT INTO Clients (SiteID, ClientName, ClientBrand, ClientSerialNr, ClientOS, PatchLevel, InstallType)
                VALUES (:site, :name, :brand, :sn, :os, :pl, :it)
                RETURNING ClientID
                """;
            UUID id = jdbc.queryForObject(sql, new MapSqlParameterSource()
                    .addValue("site", c.getSiteID())
                    .addValue("name", c.getClientName())
                    .addValue("brand", c.getClientBrand())
                    .addValue("sn", c.getClientSerialNr())
                    .addValue("os", c.getClientOS())
                    .addValue("pl", c.getPatchLevel())
                    .addValue("it", c.getInstallType()), UUID.class);
            c.setClientID(id);
        } else {
            String sql = """
                UPDATE Clients SET
                    SiteID = :site, ClientName = :name, ClientBrand = :brand, ClientSerialNr = :sn,
                    ClientOS = :os, PatchLevel = :pl, InstallType = :it
                WHERE ClientID = :id
                """;
            jdbc.update(sql, new MapSqlParameterSource()
                    .addValue("id", c.getClientID())
                    .addValue("site", c.getSiteID())
                    .addValue("name", c.getClientName())
                    .addValue("brand", c.getClientBrand())
                    .addValue("sn", c.getClientSerialNr())
                    .addValue("os", c.getClientOS())
                    .addValue("pl", c.getPatchLevel())
                    .addValue("it", c.getInstallType()));
        }
    }
}
