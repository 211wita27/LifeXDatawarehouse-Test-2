package at.htlle.freq.infrastructure.persistence;

import at.htlle.freq.domain.Account;
import at.htlle.freq.domain.AccountRepository;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcAccountRepository implements AccountRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcAccountRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Account> mapper = (rs, rowNum) ->
            new Account(UUID.fromString(rs.getString("id")), rs.getString("name"));

    @Override
    public Optional<Account> findById(UUID id) {
        String sql = "SELECT * FROM accounts WHERE id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource("id", id.toString());
        try {
            return Optional.ofNullable(jdbc.queryForObject(sql, params, mapper));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Account> findByName(String name) {
        String sql = "SELECT * FROM accounts WHERE name = :name";
        MapSqlParameterSource params = new MapSqlParameterSource("name", name);
        try {
            return Optional.ofNullable(jdbc.queryForObject(sql, params, mapper));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public void save(Account account) {
        String sql = "MERGE INTO accounts (id, name) VALUES (:id, :name)";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", account.getId().toString())
                .addValue("name", account.getName());

        jdbc.update(sql, params);
    }

    @Override
    public List<Account> findAll() {
        String sql = "SELECT * FROM accounts";
        return jdbc.query(sql, mapper);
    }

}
