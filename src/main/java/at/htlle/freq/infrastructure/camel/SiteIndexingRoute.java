package at.htlle.freq.infrastructure.camel;

import at.htlle.freq.domain.Site;
import at.htlle.freq.domain.SiteRepository;
import at.htlle.freq.infrastructure.lucene.LuceneIndexService;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component("SiteIndexingRoute")
public class SiteIndexingRoute extends RouteBuilder {

    private final SiteRepository repo;
    private final LuceneIndexService lucene;

    public SiteIndexingRoute(SiteRepository repo, LuceneIndexService lucene) {
        this.repo = repo;
        this.lucene = lucene;
    }

    @Override
    public void configure() {
        from("timer://idxSites?period=60000")
                .routeId("LuceneSitesReindex")
                .bean(repo, "findAll")
                .split(body())
                .process(ex -> {
                    Site s = ex.getIn().getBody(Site.class);
                    lucene.indexSite(
                            s.getSiteID() != null ? s.getSiteID().toString() : null,
                            s.getSiteName(),
                            s.getProjectID() != null ? s.getProjectID().toString() : null,
                            s.getAddressID() != null ? s.getAddressID().toString() : null,
                            s.getFireZone(),
                            s.getTenantCount()
                    );
                })
                .end();

        from("direct:index-site-single") // eindeutiger Endpoint
                .routeId("LuceneIndexSiteSingle")
                .process(ex -> {
                    Site s = ex.getIn().getBody(Site.class);
                    lucene.indexSite(
                            s.getSiteID() != null ? s.getSiteID().toString() : null,
                            s.getSiteName(),
                            s.getProjectID() != null ? s.getProjectID().toString() : null,
                            s.getAddressID() != null ? s.getAddressID().toString() : null,
                            s.getFireZone(),
                            s.getTenantCount()
                    );
                });
    }
}
