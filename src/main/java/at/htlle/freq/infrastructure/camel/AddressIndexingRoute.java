package at.htlle.freq.infrastructure.camel;

import at.htlle.freq.domain.Address;
import at.htlle.freq.domain.AddressRepository;
import at.htlle.freq.infrastructure.lucene.LuceneIndexService;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component("AddressIndexingRoute")
public class AddressIndexingRoute extends RouteBuilder {

    private final AddressRepository repo;
    private final LuceneIndexService lucene;

    public AddressIndexingRoute(AddressRepository repo, LuceneIndexService lucene) {
        this.repo = repo;
        this.lucene = lucene;
    }

    @Override
    public void configure() {
        // Periodisches Reindexing aller Addresses
        from("timer://idxAddresses?period=60000")
                .routeId("LuceneAddressesReindex")
                .bean(repo, "findAll")
                .split(body())
                .process(ex -> {
                    Address a = ex.getIn().getBody(Address.class);
                    lucene.indexAddress(
                            a.getAddressID() != null ? a.getAddressID().toString() : null,
                            a.getStreet(),
                            a.getCityID()
                    );
                })
                .end();

        // Indexing einer einzelnen Address
        from("direct:index-single-address")
                .routeId("LuceneIndexSingleAddress")
                .process(ex -> {
                    Address a = ex.getIn().getBody(Address.class);
                    lucene.indexAddress(
                            a.getAddressID() != null ? a.getAddressID().toString() : null,
                            a.getStreet(),
                            a.getCityID()
                    );
                });
    }
}
