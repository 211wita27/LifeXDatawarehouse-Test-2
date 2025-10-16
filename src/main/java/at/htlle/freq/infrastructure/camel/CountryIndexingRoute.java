package at.htlle.freq.infrastructure.camel;

import at.htlle.freq.domain.Country;
import at.htlle.freq.domain.CountryRepository;
import at.htlle.freq.infrastructure.lucene.LuceneIndexService;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component("CountryIndexingRoute")
public class CountryIndexingRoute extends RouteBuilder {

    private final CountryRepository repo;
    private final LuceneIndexService lucene;

    public CountryIndexingRoute(CountryRepository repo, LuceneIndexService lucene) {
        this.repo = repo;
        this.lucene = lucene;
    }

    @Override
    public void configure() {
        // Periodisches Reindexing aller Länder
        from("timer://idxCountries?period=60000")
                .routeId("LuceneCountriesReindex")
                .bean(repo, "findAll")
                .split(body())
                .process(ex -> {
                    Country c = ex.getIn().getBody(Country.class);
                    lucene.indexCountry(c.getCountryCode(), c.getCountryName());
                })
                .end();

        // Indexing eines einzelnen Landes
        from("direct:index-single-country")
                .routeId("LuceneIndexSingleCountry")
                .process(ex -> {
                    Country c = ex.getIn().getBody(Country.class);
                    lucene.indexCountry(c.getCountryCode(), c.getCountryName());
                });
    }
}
