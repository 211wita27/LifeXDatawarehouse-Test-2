package at.htlle.freq.infrastructure.camel;

import at.htlle.freq.domain.Account;
import at.htlle.freq.infrastructure.lucene.LuceneIndexService;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

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
                    String idStr = a.getAccountID() != null ? a.getAccountID().toString() : null;

                    lucene.indexAccount(
                            idStr,
                            a.getAccountName(),
                            a.getCountry(),
                            a.getContactEmail()
                    );
                })
                .log("✅ Einzelner Account indexiert: ${body.accountName}");
    }
}
