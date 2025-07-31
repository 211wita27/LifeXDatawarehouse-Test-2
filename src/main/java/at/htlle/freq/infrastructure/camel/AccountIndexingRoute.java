package at.htlle.freq.infrastructure.camel;

import at.htlle.freq.domain.Account;
import at.htlle.freq.domain.AccountRepository;
import at.htlle.freq.infrastructure.lucene.LuceneIndexService;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Indexiert alle Accounts alle 60 s inkrementell neu.
 * (Ergänzt den Full-Reindex sinnvoll für Live-Änderungen.)
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
                .log("🔁 Starte Lucene Account-Sync …")
                .bean(accountRepository, "findAll")                   // liefert List<Account>
                .split(body())                                        // 1 Account pro Message
                .process(ex -> {
                    Account a = ex.getIn().getBody(Account.class);
                    lucene.indexAccount(
                            a.getAccountID(),
                            a.getAccountName(),
                            a.getCountry()
                    );
                })
                .log("✅ Account indexiert: ${body.accountName}");
    }
}