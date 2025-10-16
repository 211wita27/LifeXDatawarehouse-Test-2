package at.htlle.freq.infrastructure.camel;

import at.htlle.freq.domain.UpgradePlan;
import at.htlle.freq.domain.UpgradePlanRepository;
import at.htlle.freq.infrastructure.lucene.LuceneIndexService;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component("UpgradePlanIndexingRoute")
public class UpgradePlanIndexingRoute extends RouteBuilder {

    private final UpgradePlanRepository repo;
    private final LuceneIndexService lucene;

    public UpgradePlanIndexingRoute(UpgradePlanRepository repo, LuceneIndexService lucene) {
        this.repo = repo;
        this.lucene = lucene;
    }

    @Override
    public void configure() {
        from("timer://idxUpgradePlans?period=60000")
                .routeId("LuceneUpgradePlansReindex")
                .bean(repo, "findAll")
                .split(body())
                .process(ex -> {
                    UpgradePlan u = ex.getIn().getBody(UpgradePlan.class);
                    lucene.indexUpgradePlan(
                            u.getUpgradePlanID() != null ? u.getUpgradePlanID().toString() : null,
                            u.getSiteID() != null ? u.getSiteID().toString() : null,
                            u.getSoftwareID() != null ? u.getSoftwareID().toString() : null,
                            u.getStatus(),
                            u.getPlannedWindowStart(),
                            u.getPlannedWindowEnd()
                    );
                })
                .end();

        from("direct:index-upgradePlan-single") // eindeutiger Endpoint
                .routeId("LuceneIndexUpgradePlanSingle")
                .process(ex -> {
                    UpgradePlan u = ex.getIn().getBody(UpgradePlan.class);
                    lucene.indexUpgradePlan(
                            u.getUpgradePlanID() != null ? u.getUpgradePlanID().toString() : null,
                            u.getSiteID() != null ? u.getSiteID().toString() : null,
                            u.getSoftwareID() != null ? u.getSoftwareID().toString() : null,
                            u.getStatus(),
                            u.getPlannedWindowStart(),
                            u.getPlannedWindowEnd()
                    );
                });
    }
}
