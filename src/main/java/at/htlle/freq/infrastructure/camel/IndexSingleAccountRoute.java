package at.htlle.freq.infrastructure.camel;
import at.htlle.freq.domain.Account;
import at.htlle.freq.infrastructure.lucene.LuceneIndexService;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;


@Component
public class IndexSingleAccountRoute extends RouteBuilder {
    private final LuceneIndexService luceneIndexService;

    public IndexSingleAccountRoute(LuceneIndexService luceneIndexService) {
        this.luceneIndexService = luceneIndexService;
    }

    @Override
    public void configure() {
        from("direct:index-account")
                .routeId("IndexSingleAccount")
                .log("📥 Indexiere einzelnen Account...")
                .process(exchange -> {
                    Account acc = exchange.getIn().getBody(Account.class);
                    luceneIndexService.index(acc);
                })
                .log("✅ Einzelner Account indexiert: ${body.name}");
    }
}
