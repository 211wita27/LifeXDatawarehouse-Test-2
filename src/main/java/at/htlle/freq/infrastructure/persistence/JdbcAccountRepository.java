package at.htlle.freq.infrastructure.persistence;

import at.htlle.freq.domain.Account;
import at.htlle.freq.domain.AccountRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class JdbcAccountRepository implements AccountRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcAccountRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Account> mapper = (rs, n) -> new Account(
            rs.getObject("AccountID", UUID.class),
            rs.getString("AccountName"),
            rs.getString("ContactName"),
            rs.getString("ContactEmail"),
            rs.getString("ContactPhone"),
            rs.getString("VATNumber"),
            rs.getString("Country")
    );

    @Override
    public Optional<Account> findById(UUID id) {
        String sql = """
            SELECT AccountID, AccountName, ContactName, ContactEmail, ContactPhone, VATNumber, Country
            FROM Account
            WHERE AccountID = :id
            """;
        try {
            return Optional.ofNullable(
                    jdbc.queryForObject(sql, new MapSqlParameterSource("id", id), mapper)
            );
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Account> findByName(String name) {
        String sql = """
            SELECT AccountID, AccountName, ContactName, ContactEmail, ContactPhone, VATNumber, Country
            FROM Account
            WHERE AccountName = :name
            """;
        try {
            return Optional.ofNullable(
                    jdbc.queryForObject(sql, new MapSqlParameterSource("name", name), mapper)
            );
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Account> findAll() {
        String sql = """
            SELECT AccountID, AccountName, ContactName, ContactEmail, ContactPhone, VATNumber, Country
            FROM Account
            """;
        return jdbc.query(sql, mapper);
    }

    @Override
    public Account save(Account a) {
        boolean isNew = (a.getAccountID() == null);

        if (isNew) {
            UUID id = UUID.randomUUID();
            a.setAccountID(id);

            String sql = """
            INSERT INTO Account (
                AccountID, AccountName, ContactName, ContactEmail, ContactPhone, VATNumber, Country
            ) VALUES (
                :id, :accountName, :contactName, :contactEmail, :contactPhone, :vatNumber, :country
            )
            """;
            var params = new MapSqlParameterSource()
                    .addValue("id",           a.getAccountID())
                    .addValue("accountName",  a.getAccountName())
                    .addValue("contactName",  a.getContactName())
                    .addValue("contactEmail", a.getContactEmail())
                    .addValue("contactPhone", a.getContactPhone())
                    .addValue("vatNumber",    a.getVatNumber())
                    .addValue("country",      a.getCountry());

            jdbc.update(sql, params);
        } else {
            String sql = """
            UPDATE Account SET
                AccountName  = :accountName,
                ContactName  = :contactName,
                ContactEmail = :contactEmail,
                ContactPhone = :contactPhone,
                VATNumber    = :vatNumber,
                Country      = :country
            WHERE AccountID = :id
            """;
            var params = new MapSqlParameterSource()
                    .addValue("id",           a.getAccountID())
                    .addValue("accountName",  a.getAccountName())
                    .addValue("contactName",  a.getContactName())
                    .addValue("contactEmail", a.getContactEmail())
                    .addValue("contactPhone", a.getContactPhone())
                    .addValue("vatNumber",    a.getVatNumber())
                    .addValue("country",      a.getCountry());

            jdbc.update(sql, params);
        }
        return a;
    }


    @Override
    public void deleteById(UUID id) {
        String sql = "DELETE FROM Account WHERE AccountID = :id";
        var params = new MapSqlParameterSource("id", id);
        jdbc.update(sql, params);
    }
}
