package at.htlle.freq.infrastructure.camel;

import at.htlle.freq.domain.Server;
import at.htlle.freq.domain.ServerRepository;
import at.htlle.freq.infrastructure.lucene.LuceneIndexService;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component("ServerIndexingRoute")
public class ServerIndexingRoute extends RouteBuilder {

    private final ServerRepository repo;
    private final LuceneIndexService lucene;

    public ServerIndexingRoute(ServerRepository repo, LuceneIndexService lucene) {
        this.repo = repo;
        this.lucene = lucene;
    }

    @Override
    public void configure() {
        from("timer://idxServers?period=60000")
                .routeId("LuceneServersReindex")
                .bean(repo, "findAll")
                .split(body())
                .process(ex -> {
                    Server s = ex.getIn().getBody(Server.class);
                    lucene.indexServer(
                            s.getServerID() != null ? s.getServerID().toString() : null,
                            s.getSiteID() != null ? s.getSiteID().toString() : null,
                            s.getServerName(),
                            s.getServerBrand(),
                            s.getServerOS(),
                            s.getVirtualPlatform(),
                            s.isHighAvailability()
                    );
                })
                .end();

        from("direct:index-server-single") // eindeutiger Endpoint
                .routeId("LuceneIndexServerSingle")
                .process(ex -> {
                    Server s = ex.getIn().getBody(Server.class);
                    lucene.indexServer(
                            s.getServerID() != null ? s.getServerID().toString() : null,
                            s.getSiteID() != null ? s.getSiteID().toString() : null,
                            s.getServerName(),
                            s.getServerBrand(),
                            s.getServerOS(),
                            s.getVirtualPlatform(),
                            s.isHighAvailability()
                    );
                });
    }
}
