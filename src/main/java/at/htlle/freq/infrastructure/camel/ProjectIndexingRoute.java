package at.htlle.freq.infrastructure.camel;

import at.htlle.freq.domain.Project;
import at.htlle.freq.domain.ProjectRepository;
import at.htlle.freq.infrastructure.lucene.LuceneIndexService;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component("ProjectIndexingRoute")
public class ProjectIndexingRoute extends RouteBuilder {

    private final ProjectRepository repo;
    private final LuceneIndexService lucene;

    public ProjectIndexingRoute(ProjectRepository repo, LuceneIndexService lucene) {
        this.repo = repo;
        this.lucene = lucene;
    }

    @Override
    public void configure() {
        from("timer://idxProjects?period=60000")
                .routeId("LuceneProjectsReindex")
                .bean(repo, "findAll")
                .split(body())
                .process(ex -> {
                    Project p = ex.getIn().getBody(Project.class);
                    lucene.indexProject(
                            p.getProjectID() != null ? p.getProjectID().toString() : null,
                            p.getProjectName(),
                            p.getProjectSAPID(),
                            p.getAccountID() != null ? p.getAccountID().toString() : null,
                            p.getDeploymentVariantID() != null ? p.getDeploymentVariantID().toString() : null,
                            p.isStillActive()
                    );
                })
                .end();

        from("direct:index-project-single") // eindeutiger Endpoint
                .routeId("LuceneIndexProjectSingle")
                .process(ex -> {
                    Project p = ex.getIn().getBody(Project.class);
                    lucene.indexProject(
                            p.getProjectID() != null ? p.getProjectID().toString() : null,
                            p.getProjectName(),
                            p.getProjectSAPID(),
                            p.getAccountID() != null ? p.getAccountID().toString() : null,
                            p.getDeploymentVariantID() != null ? p.getDeploymentVariantID().toString() : null,
                            p.isStillActive()
                    );
                });
    }
}
