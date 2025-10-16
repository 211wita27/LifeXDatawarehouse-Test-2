package at.htlle.freq.infrastructure.camel;

import at.htlle.freq.domain.InstalledSoftware;
import at.htlle.freq.domain.InstalledSoftwareRepository;
import at.htlle.freq.infrastructure.lucene.LuceneIndexService;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component("InstalledSoftwareIndexingRoute")
public class InstalledSoftwareIndexingRoute extends RouteBuilder {

    private final InstalledSoftwareRepository repo;
    private final LuceneIndexService lucene;

    public InstalledSoftwareIndexingRoute(InstalledSoftwareRepository repo, LuceneIndexService lucene) {
        this.repo = repo;
        this.lucene = lucene;
    }

    @Override
    public void configure() {
        from("timer://idxInstalledSoftware?period=60000")
                .routeId("LuceneInstalledSoftwareReindex")
                .bean(repo, "findAll")
                .split(body())
                .process(ex -> {
                    InstalledSoftware is = ex.getIn().getBody(InstalledSoftware.class);
                    lucene.indexInstalledSoftware(
                            is.getInstalledSoftwareID() != null ? is.getInstalledSoftwareID().toString() : null,
                            is.getSiteID() != null ? is.getSiteID().toString() : null,
                            is.getSoftwareID() != null ? is.getSoftwareID().toString() : null
                    );
                })
                .end();

        from("direct:index-installedSoftware-single") // eindeutiger Endpoint
                .routeId("LuceneIndexInstalledSoftwareSingle")
                .process(ex -> {
                    InstalledSoftware is = ex.getIn().getBody(InstalledSoftware.class);
                    lucene.indexInstalledSoftware(
                            is.getInstalledSoftwareID() != null ? is.getInstalledSoftwareID().toString() : null,
                            is.getSiteID() != null ? is.getSiteID().toString() : null,
                            is.getSoftwareID() != null ? is.getSoftwareID().toString() : null
                    );
                });
    }
}
