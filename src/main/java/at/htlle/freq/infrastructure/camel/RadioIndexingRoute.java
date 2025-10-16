package at.htlle.freq.infrastructure.camel;

import at.htlle.freq.domain.Radio;
import at.htlle.freq.domain.RadioRepository;
import at.htlle.freq.infrastructure.lucene.LuceneIndexService;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component("RadioIndexingRoute")
public class RadioIndexingRoute extends RouteBuilder {

    private final RadioRepository repo;
    private final LuceneIndexService lucene;

    public RadioIndexingRoute(RadioRepository repo, LuceneIndexService lucene) {
        this.repo = repo;
        this.lucene = lucene;
    }

    @Override
    public void configure() {
        from("timer://idxRadios?period=60000")
                .routeId("LuceneRadiosReindex")
                .bean(repo, "findAll")
                .split(body())
                .process(ex -> {
                    Radio r = ex.getIn().getBody(Radio.class);
                    lucene.indexRadio(
                            r.getRadioID() != null ? r.getRadioID().toString() : null,
                            r.getSiteID() != null ? r.getSiteID().toString() : null,
                            r.getAssignedClientID() != null ? r.getAssignedClientID().toString() : null,
                            r.getRadioBrand(),
                            r.getMode(),
                            r.getDigitalStandard()
                    );
                })
                .end();

        from("direct:index-radio-single") // eindeutiger Endpoint
                .routeId("LuceneIndexRadioSingle")
                .process(ex -> {
                    Radio r = ex.getIn().getBody(Radio.class);
                    lucene.indexRadio(
                            r.getRadioID() != null ? r.getRadioID().toString() : null,
                            r.getSiteID() != null ? r.getSiteID().toString() : null,
                            r.getAssignedClientID() != null ? r.getAssignedClientID().toString() : null,
                            r.getRadioBrand(),
                            r.getMode(),
                            r.getDigitalStandard()
                    );
                });
    }
}
