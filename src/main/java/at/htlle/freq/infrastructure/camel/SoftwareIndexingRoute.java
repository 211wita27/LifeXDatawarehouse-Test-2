package at.htlle.freq.infrastructure.camel;

import at.htlle.freq.domain.Software;
import at.htlle.freq.domain.SoftwareRepository;
import at.htlle.freq.infrastructure.lucene.LuceneIndexService;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component("SoftwareIndexingRoute")
public class SoftwareIndexingRoute extends RouteBuilder {

    private final SoftwareRepository repo;
    private final LuceneIndexService lucene;

    public SoftwareIndexingRoute(SoftwareRepository repo, LuceneIndexService lucene) {
        this.repo = repo;
        this.lucene = lucene;
    }

    @Override
    public void configure() {
        from("timer://idxSoftware?period=60000")
                .routeId("LuceneSoftwareReindex")
                .bean(repo, "findAll")
                .split(body())
                .process(ex -> {
                    Software s = ex.getIn().getBody(Software.class);
                    lucene.indexSoftware(
                            s.getSoftwareID() != null ? s.getSoftwareID().toString() : null,
                            s.getName(),
                            s.getRelease(),
                            s.getRevision(),
                            s.getSupportPhase()
                    );
                })
                .end();

        from("direct:index-software-single") // eindeutiger Endpoint
                .routeId("LuceneIndexSoftwareSingle")
                .process(ex -> {
                    Software s = ex.getIn().getBody(Software.class);
                    lucene.indexSoftware(
                            s.getSoftwareID() != null ? s.getSoftwareID().toString() : null,
                            s.getName(),
                            s.getRelease(),
                            s.getRevision(),
                            s.getSupportPhase()
                    );
                });
    }
}
