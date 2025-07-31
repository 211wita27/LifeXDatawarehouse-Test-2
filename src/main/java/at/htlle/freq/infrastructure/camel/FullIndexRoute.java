package at.htlle.freq.infrastructure.camel;

import at.htlle.freq.infrastructure.lucene.LuceneIndexService;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Re-indexiert ALLE Tabellen alle 60 s.
 * (Für Demo völlig ausreichend; in Produktion besser Events nutzen.)
 */
@Component
public class FullIndexRoute extends RouteBuilder {

    private final LuceneIndexService lucene;

    public FullIndexRoute(LuceneIndexService lucene) {
        this.lucene = lucene;
    }

    @Override
    public void configure() {
        from("timer://fullReindex?period=60000")           // 60 000 ms
                .routeId("FullLuceneReindex")
                .log("🔄 Re-Index ALL tables …")
                .bean(lucene, "reindexAll")
                .log("✅ Re-Index done.");
    }
}