package at.htlle.freq.infrastructure.camel;

import at.htlle.freq.domain.AccountRepository;
import at.htlle.freq.infrastructure.lucene.LuceneIndexService;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class AccountIndexingRoute extends RouteBuilder {

    private final AccountRepository accountRepository;
    private final LuceneIndexService luceneIndexService;

    public AccountIndexingRoute(AccountRepository accountRepository, LuceneIndexService luceneIndexService) {
        this.accountRepository = accountRepository;
        this.luceneIndexService = luceneIndexService;
    }

    @Override
    public void configure() {
        from("timer:indexAccounts?period=60000") // alle 60 Sekunden
                .routeId("LuceneAccountSync")
                .log("🔁 Starte Lucene Sync...")
                .bean(accountRepository, "findAll") // findAll() musst du noch hinzufügen!
                .split(body())
                .process(exchange -> {
                    var acc = exchange.getIn().getBody(at.htlle.freq.domain.Account.class);
                    luceneIndexService.index(acc);
                })
                .log("✅ Account indexiert: ${body.name}");
    }
}
