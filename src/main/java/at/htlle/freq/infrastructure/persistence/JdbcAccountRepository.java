package at.htlle.freq.infrastructure.persistence;

import at.htlle.freq.domain.Account;
import at.htlle.freq.domain.AccountRepository;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JdbcAccountRepository implements AccountRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcAccountRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Account> mapper = (rs, rowNum) ->
            new Account(
                    rs.getInt("AccountID"),
                    rs.getString("AccountName"),
                    rs.getString("ContactEmail"),
                    rs.getString("ContactPhone"),
                    rs.getString("VATNumber"),
                    rs.getString("Country")
            );

    @Override
    public Optional<Account> findById(int id) {
        String sql = """
            SELECT
              AccountID,
              AccountName,
              ContactEmail,
              ContactPhone,
              VATNumber,
              Country
            FROM Account
            WHERE AccountID = :id
            """;
        var params = new MapSqlParameterSource("id", id);
        try {
            return Optional.ofNullable(jdbc.queryForObject(sql, params, mapper));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Account> findByName(String name) {
        String sql = """
            SELECT
              AccountID,
              AccountName,
              ContactEmail,
              ContactPhone,
              VATNumber,
              Country
            FROM Account
            WHERE AccountName = :name
            """;
        var params = new MapSqlParameterSource("name", name);
        try {
            return Optional.ofNullable(jdbc.queryForObject(sql, params, mapper));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Account> findAll() {
        String sql = """
            SELECT
              AccountID,
              AccountName,
              ContactEmail,
              ContactPhone,
              VATNumber,
              Country
            FROM Account
            """;
        return jdbc.query(sql, mapper);
    }

    @Override
    public void save(Account account) {
        String sql = """
            MERGE INTO Account (
              AccountID,
              AccountName,
              ContactName,
              ContactEmail,
              ContactPhone,
              VATNumber,
              Country
            ) VALUES (
              :id,
              :accountName,
              :contactName,
              :contactEmail,
              :contactPhone,
              :vatNumber,
              :country
            )
            """;

        var params = new MapSqlParameterSource()
                .addValue("id",              account.getAccountID())
                .addValue("accountName",     account.getAccountName())
                // weil ContactName NOT NULL ist, hier einfach mit AccountName vorbelegen
                .addValue("contactName",     account.getAccountName())
                .addValue("contactEmail",    account.getContactEmail())
                .addValue("contactPhone",    account.getContactPhone())
                .addValue("vatNumber",       account.getVATNumber())
                .addValue("country",         account.getCountry());

        jdbc.update(sql, params);
    }
}
