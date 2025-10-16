package at.htlle.freq.infrastructure.camel;

import at.htlle.freq.domain.DeploymentVariant;
import at.htlle.freq.domain.DeploymentVariantRepository;
import at.htlle.freq.infrastructure.lucene.LuceneIndexService;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component("DeploymentVariantIndexingRoute")
public class DeploymentVariantIndexingRoute extends RouteBuilder {

    private final DeploymentVariantRepository repo;
    private final LuceneIndexService lucene;

    public DeploymentVariantIndexingRoute(DeploymentVariantRepository repo, LuceneIndexService lucene) {
        this.repo = repo;
        this.lucene = lucene;
    }

    @Override
    public void configure() {
        // Periodisches Reindexing aller Deployment Variants
        from("timer://idxDeploymentVariants?period=60000")
                .routeId("LuceneDeploymentVariantsReindex")
                .bean(repo, "findAll")
                .split(body())
                .process(ex -> {
                    DeploymentVariant dv = ex.getIn().getBody(DeploymentVariant.class);
                    lucene.indexDeploymentVariant(
                            dv.getVariantID() != null ? dv.getVariantID().toString() : null,
                            dv.getVariantCode(),
                            dv.getVariantName(),
                            dv.isActive()
                    );
                })
                .end();

        // Indexing eines einzelnen Deployment Variants
        from("direct:index-single-deployment-variant")
                .routeId("LuceneIndexSingleDeploymentVariant")
                .process(ex -> {
                    DeploymentVariant dv = ex.getIn().getBody(DeploymentVariant.class);
                    lucene.indexDeploymentVariant(
                            dv.getVariantID() != null ? dv.getVariantID().toString() : null,
                            dv.getVariantCode(),
                            dv.getVariantName(),
                            dv.isActive()
                    );
                });
    }
}
