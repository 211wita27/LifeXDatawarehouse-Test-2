package at.htlle.freq.infrastructure.persistence;

import at.htlle.freq.domain.City;
import at.htlle.freq.domain.CityRepository;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class JdbcCityRepository implements CityRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcCityRepository(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }

    private final RowMapper<City> mapper = (rs, n) -> new City(
            rs.getString("CityID"),
            rs.getString("CityName"),
            rs.getString("CountryCode")
    );

    @Override
    public Optional<City> findById(String id) {
        String sql = "SELECT CityID, CityName, CountryCode FROM City WHERE CityID = :id";
        try {
            return Optional.ofNullable(jdbc.queryForObject(sql, new MapSqlParameterSource("id", id), mapper));
        } catch (Exception e) { return Optional.empty(); }
    }

    @Override
    public List<City> findByCountry(String countryCode) {
        String sql = "SELECT CityID, CityName, CountryCode FROM City WHERE CountryCode = :cc";
        return jdbc.query(sql, new MapSqlParameterSource("cc", countryCode), mapper);
    }

    @Override
    public void deleteById(String id) {
        String sql = "DELETE FROM City WHERE CityID = :id";
        jdbc.update(sql, new MapSqlParameterSource("id", id));
    }

    @Override
    public City save(City c) {
        // Upsert über EXISTS
        String existsSql = "SELECT COUNT(*) FROM City WHERE CityID = :id";
        boolean exists = Boolean.TRUE.equals(
                jdbc.queryForObject(existsSql, new MapSqlParameterSource("id", c.getCityID()), Integer.class)
        ) && jdbc.queryForObject(existsSql, new MapSqlParameterSource("id", c.getCityID()), Integer.class) > 0;

        if (!exists) {
            String ins = """
                INSERT INTO City (CityID, CityName, CountryCode)
                VALUES (:id, :name, :cc)
                """;
            jdbc.update(ins, new MapSqlParameterSource()
                    .addValue("id", c.getCityID())
                    .addValue("name", c.getCityName())
                    .addValue("cc", c.getCountryCode()));
        } else {
            String upd = """
                UPDATE City SET CityName = :name, CountryCode = :cc
                WHERE CityID = :id
                """;
            jdbc.update(upd, new MapSqlParameterSource()
                    .addValue("id", c.getCityID())
                    .addValue("name", c.getCityName())
                    .addValue("cc", c.getCountryCode()));
        }
        return c;
    }

    @Override
    public List<City> findAll() {
        return jdbc.query("SELECT CityID, CityName, CountryCode FROM City", mapper);
    }
}
