package at.htlle.freq.infrastructure.camel;

import at.htlle.freq.domain.City;
import at.htlle.freq.domain.CityRepository;
import at.htlle.freq.infrastructure.lucene.LuceneIndexService;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component("CityIndexingRoute")
public class CityIndexingRoute extends RouteBuilder {

    private final CityRepository repo;
    private final LuceneIndexService lucene;

    public CityIndexingRoute(CityRepository repo, LuceneIndexService lucene) {
        this.repo = repo;
        this.lucene = lucene;
    }

    @Override
    public void configure() {
        // Periodisches Reindexing aller Cities
        from("timer://idxCities?period=60000")
                .routeId("LuceneCitiesReindex")
                .bean(repo, "findAll")
                .split(body())
                .process(ex -> {
                    City c = ex.getIn().getBody(City.class);
                    lucene.indexCity(c.getCityID(), c.getCityName(), c.getCountryCode());
                })
                .end();

        // Indexing einer einzelnen City
        from("direct:index-single-city")
                .routeId("LuceneIndexSingleCity")
                .process(ex -> {
                    City c = ex.getIn().getBody(City.class);
                    lucene.indexCity(c.getCityID(), c.getCityName(), c.getCountryCode());
                });
    }
}
