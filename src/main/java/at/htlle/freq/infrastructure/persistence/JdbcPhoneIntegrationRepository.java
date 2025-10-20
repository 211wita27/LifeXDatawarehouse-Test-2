package at.htlle.freq.infrastructure.persistence;

import at.htlle.freq.domain.PhoneIntegration;
import at.htlle.freq.domain.PhoneIntegrationRepository;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class JdbcPhoneIntegrationRepository implements PhoneIntegrationRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcPhoneIntegrationRepository(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }

    private final RowMapper<PhoneIntegration> mapper = (rs, n) -> new PhoneIntegration(
            rs.getObject("PhoneIntegrationID", UUID.class),
            rs.getObject("ClientID", UUID.class),
            rs.getString("PhoneType"),
            rs.getString("PhoneBrand"),
            rs.getString("PhoneSerialNr"),
            rs.getString("PhoneFirmware")
    );

    @Override
    public Optional<PhoneIntegration> findById(UUID id) {
        String sql = """
            SELECT PhoneIntegrationID, ClientID, PhoneType, PhoneBrand, PhoneSerialNr, PhoneFirmware
            FROM PhoneIntegration WHERE PhoneIntegrationID = :id
            """;
        try { return Optional.ofNullable(jdbc.queryForObject(sql, new MapSqlParameterSource("id", id), mapper)); }
        catch (Exception e) { return Optional.empty(); }
    }

    @Override
    public List<PhoneIntegration> findByClient(UUID clientId) {
        String sql = """
            SELECT PhoneIntegrationID, ClientID, PhoneType, PhoneBrand, PhoneSerialNr, PhoneFirmware
            FROM PhoneIntegration WHERE ClientID = :cid
            """;
        return jdbc.query(sql, new MapSqlParameterSource("cid", clientId), mapper);
    }

    @Override
    public List<PhoneIntegration> findAll() {
        return jdbc.query("""
            SELECT PhoneIntegrationID, ClientID, PhoneType, PhoneBrand, PhoneSerialNr, PhoneFirmware
            FROM PhoneIntegration
            """, mapper);
    }

    @Override
    public PhoneIntegration save(PhoneIntegration p) {
        boolean isNew = p.getPhoneIntegrationID() == null;
        if (isNew) {
            String sql = """
                INSERT INTO PhoneIntegration (ClientID, PhoneType, PhoneBrand, PhoneSerialNr, PhoneFirmware)
                VALUES (:client, :type, :brand, :sn, :fw)
                RETURNING PhoneIntegrationID
                """;
            UUID id = jdbc.queryForObject(sql, new MapSqlParameterSource()
                    .addValue("client", p.getClientID())
                    .addValue("type", p.getPhoneType())
                    .addValue("brand", p.getPhoneBrand())
                    .addValue("sn", p.getPhoneSerialNr())
                    .addValue("fw", p.getPhoneFirmware()), UUID.class);
            p.setPhoneIntegrationID(id);
        } else {
            String sql = """
                UPDATE PhoneIntegration SET
                    ClientID = :client, PhoneType = :type, PhoneBrand = :brand,
                    PhoneSerialNr = :sn, PhoneFirmware = :fw
                WHERE PhoneIntegrationID = :id
                """;
            jdbc.update(sql, new MapSqlParameterSource()
                    .addValue("id", p.getPhoneIntegrationID())
                    .addValue("client", p.getClientID())
                    .addValue("type", p.getPhoneType())
                    .addValue("brand", p.getPhoneBrand())
                    .addValue("sn", p.getPhoneSerialNr())
                    .addValue("fw", p.getPhoneFirmware()));
        }
        return p;
    }
}
