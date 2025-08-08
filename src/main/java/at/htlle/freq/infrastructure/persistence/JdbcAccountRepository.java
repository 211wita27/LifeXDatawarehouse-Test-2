package at.htlle.freq.infrastructure.persistence;

import at.htlle.freq.domain.Account;
import at.htlle.freq.domain.AccountRepository;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
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
            SELECT AccountID, AccountName, ContactEmail, ContactPhone, VATNumber, Country
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
            SELECT AccountID, AccountName, ContactEmail, ContactPhone, VATNumber, Country
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
            SELECT AccountID, AccountName, ContactEmail, ContactPhone, VATNumber, Country
            FROM Account
            """;
        return jdbc.query(sql, mapper);
    }

    /**
     * Speichert Account:
     * - Wenn ID == 0 → INSERT und generierte ID zurückschreiben
     * - Sonst → UPDATE
     */
    @Override
    public void save(Account account) {
        boolean isNew = account.getAccountID() == 0;

        if (isNew) {
            String sql = """
                INSERT INTO Account (
                    AccountName, ContactName, ContactEmail, ContactPhone, VATNumber, Country
                ) VALUES (
                    :accountName, :contactName, :contactEmail, :contactPhone, :vatNumber, :country
                )
                """;

            var params = new MapSqlParameterSource()
                    .addValue("accountName",  account.getAccountName())
                    // ContactName ist NOT NULL → pragmatisch mit AccountName vorbelegen
                    .addValue("contactName",  account.getAccountName())
                    .addValue("contactEmail", account.getContactEmail())
                    .addValue("contactPhone", account.getContactPhone())
                    .addValue("vatNumber",    account.getVATNumber())
                    .addValue("country",      account.getCountry());

            KeyHolder kh = new GeneratedKeyHolder();
            jdbc.update(sql, params, kh, new String[]{"AccountID"});
            Number key = kh.getKey();
            if (key != null) {
                // Lombok @Data generiert setAccountID()
                account.setAccountID(key.intValue());
            }
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
                    .addValue("id",           account.getAccountID())
                    .addValue("accountName",  account.getAccountName())
                    .addValue("contactName",  account.getAccountName())
                    .addValue("contactEmail", account.getContactEmail())
                    .addValue("contactPhone", account.getContactPhone())
                    .addValue("vatNumber",    account.getVATNumber())
                    .addValue("country",      account.getCountry());

            jdbc.update(sql, params);
        }
    }
}