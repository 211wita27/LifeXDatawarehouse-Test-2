package at.htlle.freq.infrastructure.camel;

import at.htlle.freq.domain.PhoneIntegration;
import at.htlle.freq.domain.PhoneIntegrationRepository;
import at.htlle.freq.infrastructure.lucene.LuceneIndexService;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component("PhoneIntegrationIndexingRoute")
public class PhoneIntegrationIndexingRoute extends RouteBuilder {

    private final PhoneIntegrationRepository repo;
    private final LuceneIndexService lucene;

    public PhoneIntegrationIndexingRoute(PhoneIntegrationRepository repo, LuceneIndexService lucene) {
        this.repo = repo;
        this.lucene = lucene;
    }

    @Override
    public void configure() {
        from("timer://idxPhoneIntegrations?period=60000")
                .routeId("LucenePhoneIntegrationsReindex")
                .bean(repo, "findAll")
                .split(body())
                .process(ex -> {
                    PhoneIntegration p = ex.getIn().getBody(PhoneIntegration.class);
                    lucene.indexPhoneIntegration(
                            p.getPhoneIntegrationID() != null ? p.getPhoneIntegrationID().toString() : null,
                            p.getClientID() != null ? p.getClientID().toString() : null,
                            p.getPhoneType(),
                            p.getPhoneBrand()
                    );
                })
                .end();

        from("direct:index-phoneIntegration-single") // eindeutiger Endpoint
                .routeId("LuceneIndexPhoneIntegrationSingle")
                .process(ex -> {
                    PhoneIntegration p = ex.getIn().getBody(PhoneIntegration.class);
                    lucene.indexPhoneIntegration(
                            p.getPhoneIntegrationID() != null ? p.getPhoneIntegrationID().toString() : null,
                            p.getClientID() != null ? p.getClientID().toString() : null,
                            p.getPhoneType(),
                            p.getPhoneBrand()
                    );
                });
    }
}
