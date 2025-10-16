package at.htlle.freq.infrastructure.camel;

import at.htlle.freq.domain.Clients;
import at.htlle.freq.domain.ClientsRepository;
import at.htlle.freq.infrastructure.lucene.LuceneIndexService;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component("ClientsIndexingRoute")
public class ClientsIndexingRoute extends RouteBuilder {

    private final ClientsRepository repo;
    private final LuceneIndexService lucene;

    public ClientsIndexingRoute(ClientsRepository repo, LuceneIndexService lucene) {
        this.repo = repo;
        this.lucene = lucene;
    }

    @Override
    public void configure() {
        // Periodisches Reindexing aller Clients
        from("timer://idxClients?period=60000")
                .routeId("LuceneClientsReindex")
                .bean(repo, "findAll")
                .split(body())
                .process(ex -> {
                    Clients c = ex.getIn().getBody(Clients.class);
                    lucene.indexClient(
                            c.getClientID() != null ? c.getClientID().toString() : null,
                            c.getSiteID() != null ? c.getSiteID().toString() : null,
                            c.getClientName(),
                            c.getClientBrand(),
                            c.getClientOS(),
                            c.getInstallType()
                    );
                })
                .end();

        // Indexing eines einzelnen Clients
        from("direct:index-single-client")
                .routeId("LuceneIndexSingleClient")
                .process(ex -> {
                    Clients c = ex.getIn().getBody(Clients.class);
                    lucene.indexClient(
                            c.getClientID() != null ? c.getClientID().toString() : null,
                            c.getSiteID() != null ? c.getSiteID().toString() : null,
                            c.getClientName(),
                            c.getClientBrand(),
                            c.getClientOS(),
                            c.getInstallType()
                    );
                });
    }
}
