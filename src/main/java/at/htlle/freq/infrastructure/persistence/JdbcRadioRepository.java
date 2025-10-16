package at.htlle.freq.infrastructure.persistence;

import at.htlle.freq.domain.Radio;
import at.htlle.freq.domain.RadioRepository;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class JdbcRadioRepository implements RadioRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcRadioRepository(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }

    private final RowMapper<Radio> mapper = (rs, n) -> new Radio(
            rs.getObject("RadioID", UUID.class),
            rs.getObject("SiteID", UUID.class),
            rs.getObject("AssignedClientID", UUID.class),
            rs.getString("RadioBrand"),
            rs.getString("RadioSerialNr"),
            rs.getString("Mode"),
            rs.getString("DigitalStandard")
    );

    @Override
    public Optional<Radio> findById(UUID id) {
        String sql = """
            SELECT RadioID, SiteID, AssignedClientID, RadioBrand, RadioSerialNr, Mode, DigitalStandard
            FROM Radio WHERE RadioID = :id
            """;
        try { return Optional.ofNullable(jdbc.queryForObject(sql, new MapSqlParameterSource("id", id), mapper)); }
        catch (Exception e) { return Optional.empty(); }
    }

    @Override
    public List<Radio> findBySite(UUID siteId) {
        String sql = """
            SELECT RadioID, SiteID, AssignedClientID, RadioBrand, RadioSerialNr, Mode, DigitalStandard
            FROM Radio WHERE SiteID = :sid
            """;
        return jdbc.query(sql, new MapSqlParameterSource("sid", siteId), mapper);
    }

    @Override
    public List<Radio> findAll() {
        return jdbc.query("""
            SELECT RadioID, SiteID, AssignedClientID, RadioBrand, RadioSerialNr, Mode, DigitalStandard
            FROM Radio
            """, mapper);
    }

    @Override
    public void save(Radio r) {
        boolean isNew = r.getRadioID() == null;
        if (isNew) {
            String sql = """
                INSERT INTO Radio (SiteID, AssignedClientID, RadioBrand, RadioSerialNr, Mode, DigitalStandard)
                VALUES (:site, :client, :brand, :sn, :mode, :ds)
                RETURNING RadioID
                """;
            UUID id = jdbc.queryForObject(sql, new MapSqlParameterSource()
                    .addValue("site", r.getSiteID())
                    .addValue("client", r.getAssignedClientID())
                    .addValue("brand", r.getRadioBrand())
                    .addValue("sn", r.getRadioSerialNr())
                    .addValue("mode", r.getMode())
                    .addValue("ds", r.getDigitalStandard()), UUID.class);
            r.setRadioID(id);
        } else {
            String sql = """
                UPDATE Radio SET
                    SiteID = :site, AssignedClientID = :client, RadioBrand = :brand, RadioSerialNr = :sn,
                    Mode = :mode, DigitalStandard = :ds
                WHERE RadioID = :id
                """;
            jdbc.update(sql, new MapSqlParameterSource()
                    .addValue("id", r.getRadioID())
                    .addValue("site", r.getSiteID())
                    .addValue("client", r.getAssignedClientID())
                    .addValue("brand", r.getRadioBrand())
                    .addValue("sn", r.getRadioSerialNr())
                    .addValue("mode", r.getMode())
                    .addValue("ds", r.getDigitalStandard()));
        }
    }
}
