package at.htlle.freq.infrastructure.camel;

import at.htlle.freq.domain.Account;
import at.htlle.freq.infrastructure.lucene.LuceneIndexService;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Wird von AccountService nach jedem INSERT / UPDATE
 * mit <code>camel.sendBody("direct:index-account", account)</code> aufgerufen.
 */
@Component
public class IndexSingleAccountRoute extends RouteBuilder {

    private final LuceneIndexService lucene;

    public IndexSingleAccountRoute(LuceneIndexService lucene) {
        this.lucene = lucene;
    }

    @Override
    public void configure() {

        from("direct:index-account")
                .routeId("IndexSingleAccount")
                .process(ex -> {
                    Account a = ex.getIn().getBody(Account.class);
                    lucene.indexAccount(
                            a.getAccountID(),
                            a.getAccountName(),
                            a.getCountry()
                    );
                })
                .log("✅ Einzelner Account indexiert: ${body.accountName}");
    }
}