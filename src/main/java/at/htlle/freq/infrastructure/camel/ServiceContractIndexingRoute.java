package at.htlle.freq.infrastructure.camel;

import at.htlle.freq.domain.ServiceContract;
import at.htlle.freq.domain.ServiceContractRepository;
import at.htlle.freq.infrastructure.lucene.LuceneIndexService;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component("ServiceContractIndexingRoute")
public class ServiceContractIndexingRoute extends RouteBuilder {

    private final ServiceContractRepository repo;
    private final LuceneIndexService lucene;

    public ServiceContractIndexingRoute(ServiceContractRepository repo, LuceneIndexService lucene) {
        this.repo = repo;
        this.lucene = lucene;
    }

    @Override
    public void configure() {
        from("timer://idxServiceContracts?period=60000")
                .routeId("LuceneServiceContractsReindex")
                .bean(repo, "findAll")
                .split(body())
                .process(ex -> {
                    ServiceContract sc = ex.getIn().getBody(ServiceContract.class);
                    lucene.indexServiceContract(
                            sc.getContractID() != null ? sc.getContractID().toString() : null,
                            sc.getAccountID() != null ? sc.getAccountID().toString() : null,
                            sc.getProjectID() != null ? sc.getProjectID().toString() : null,
                            sc.getSiteID() != null ? sc.getSiteID().toString() : null,
                            sc.getContractNumber(),
                            sc.getStatus()
                    );
                })
                .end();

        from("direct:index-serviceContract-single") // eindeutiger Endpoint
                .routeId("LuceneIndexServiceContractSingle")
                .process(ex -> {
                    ServiceContract sc = ex.getIn().getBody(ServiceContract.class);
                    lucene.indexServiceContract(
                            sc.getContractID() != null ? sc.getContractID().toString() : null,
                            sc.getAccountID() != null ? sc.getAccountID().toString() : null,
                            sc.getProjectID() != null ? sc.getProjectID().toString() : null,
                            sc.getSiteID() != null ? sc.getSiteID().toString() : null,
                            sc.getContractNumber(),
                            sc.getStatus()
                    );
                });
    }
}
