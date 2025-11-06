package at.htlle.freq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the LifeX Datawarehouse application.
 * <p>
 * Enables the {@code default} Spring profile and starts the Camel routes
 * {@link at.htlle.freq.infrastructure.camel.UnifiedIndexingRoutes} and
 * {@link at.htlle.freq.infrastructure.camel.LuceneIndexingHubRoute}. Together with the
 * {@link at.htlle.freq.infrastructure.lucene.LuceneIndexService LuceneIndexService}
 * they maintain the search index. Configuration (e.g., data source, Lucene flags, and
 * profiles) is centrally managed in {@code application.yml}.
 * </p>
 */
@SpringBootApplication
public class LifeXDatawrehouseAppApplication {

    /**
     * Starts the Spring Boot application.
     * <p>
     * Requires a reachable database and write access to the Lucene index path
     * ({@code target/lifex-index}); if the Camel-Lucene setup is disabled (property
     * {@code lifex.lucene.camel.enabled=false}), reindex calls must be triggered manually.
     * </p>
     */
    public static void main(String[] args) {
        SpringApplication.run(LifeXDatawrehouseAppApplication.class, args);
    }
}
