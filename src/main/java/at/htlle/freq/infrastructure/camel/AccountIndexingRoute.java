package at.htlle.freq.infrastructure.camel;

import at.htlle.freq.domain.Account;
import at.htlle.freq.domain.AccountRepository;
import at.htlle.freq.infrastructure.lucene.LuceneIndexService;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component("AccountIndexingRoute")
public class AccountIndexingRoute extends RouteBuilder {

    private final AccountRepository repo;
    private final LuceneIndexService lucene;

    public AccountIndexingRoute(AccountRepository repo, LuceneIndexService lucene) {
        this.repo = repo;
        this.lucene = lucene;
    }

    @Override
    public void configure() {
        // Periodisches Reindexing aller Accounts
        from("timer://idxAccounts?period=60000")
                .routeId("LuceneAccountsReindex")
                .bean(repo, "findAll")
                .split(body())
                .process(ex -> {
                    Account a = ex.getIn().getBody(Account.class);
                    lucene.indexAccount(
                            a.getAccountID() != null ? a.getAccountID().toString() : null,
                            a.getAccountName(),
                            a.getCountry(),
                            a.getContactEmail()
                    );
                })
                .end();

        // Indexing eines einzelnen Accounts
        from("direct:index-single-account")
                .routeId("LuceneIndexSingleAccount")
                .process(ex -> {
                    Account a = ex.getIn().getBody(Account.class);
                    lucene.indexAccount(
                            a.getAccountID() != null ? a.getAccountID().toString() : null,
                            a.getAccountName(),
                            a.getCountry(),
                            a.getContactEmail()
                    );
                });
    }
}
