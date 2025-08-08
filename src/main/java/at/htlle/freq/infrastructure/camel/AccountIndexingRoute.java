package at.htlle.freq.infrastructure.camel;

import at.htlle.freq.domain.Account;
import at.htlle.freq.domain.AccountRepository;
import at.htlle.freq.infrastructure.lucene.LuceneIndexService;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Indexiert alle Accounts alle 60 s inkrementell neu.
 * Log-Ausgabe: Start + Zusammenfassung, kein Spam pro Account.
 */
@Component
public class AccountIndexingRoute extends RouteBuilder {

    private final AccountRepository  accountRepository;
    private final LuceneIndexService lucene;

    public AccountIndexingRoute(AccountRepository accountRepository,
                                LuceneIndexService lucene) {
        this.accountRepository = accountRepository;
        this.lucene           = lucene;
    }

    @Override
    public void configure() {

        from("timer://indexAccounts?period=60000")              // alle 60 s
                .routeId("LuceneAccountSync")
                .log("🔁 Lucene Account-Sync gestartet …")
                .bean(accountRepository, "findAll")                 // List<Account>
                .process(ex -> ex.setProperty("cnt", 0))            // Zähler zurücksetzen
                .split(body())
                .process(ex -> {
                    Account a = ex.getIn().getBody(Account.class);
                    lucene.indexAccount(
                            a.getAccountID(),
                            a.getAccountName(),
                            a.getCountry(),
                            a.getContactEmail()
                    );
                    Integer c = ex.getProperty("cnt", Integer.class);
                    ex.setProperty("cnt", (c == null ? 1 : c + 1));
                })
                .end()
                .log("✅ Lucene Account-Sync fertig: ${exchangeProperty.cnt} Accounts aktualisiert");
    }
}
