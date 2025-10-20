package at.htlle.freq.infrastructure.persistence;

import at.htlle.freq.domain.Country;
import at.htlle.freq.domain.CountryRepository;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class JdbcCountryRepository implements CountryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcCountryRepository(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }

    private final RowMapper<Country> mapper = (rs, n) -> new Country(
            rs.getString("CountryCode"),
            rs.getString("CountryName")
    );

    @Override
    public Optional<Country> findById(String code) {
        String sql = "SELECT CountryCode, CountryName FROM Country WHERE CountryCode = :code";
        try {
            return Optional.ofNullable(jdbc.queryForObject(sql, new MapSqlParameterSource("code", code), mapper));
        } catch (Exception e) { return Optional.empty(); }
    }

    @Override
    public Country save(Country c) {
        String existsSql = "SELECT COUNT(*) FROM Country WHERE CountryCode = :code";
        Integer cnt = jdbc.queryForObject(existsSql, new MapSqlParameterSource("code", c.getCountryCode()), Integer.class);
        boolean exists = cnt != null && cnt > 0;

        if (!exists) {
            String ins = "INSERT INTO Country (CountryCode, CountryName) VALUES (:code, :name)";
            jdbc.update(ins, new MapSqlParameterSource()
                    .addValue("code", c.getCountryCode())
                    .addValue("name", c.getCountryName()));
        } else {
            String upd = "UPDATE Country SET CountryName = :name WHERE CountryCode = :code";
            jdbc.update(upd, new MapSqlParameterSource()
                    .addValue("code", c.getCountryCode())
                    .addValue("name", c.getCountryName()));
        }
        return c;
    }

    @Override
    public List<Country> findAll() {
        return jdbc.query("SELECT CountryCode, CountryName FROM Country", mapper);
    }
}
