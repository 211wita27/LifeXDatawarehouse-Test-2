package at.htlle.freq.application;

import at.htlle.freq.domain.ProjectSiteAssignmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ProjectSiteAssignmentService {

    private final ProjectSiteAssignmentRepository repository;

    public ProjectSiteAssignmentService(ProjectSiteAssignmentRepository repository) {
        this.repository = repository;
    }

    public List<UUID> getProjectsForSite(UUID siteId) {
        Objects.requireNonNull(siteId, "siteId must not be null");
        return repository.findProjectIdsBySite(siteId);
    }

    public List<UUID> getSitesForProject(UUID projectId) {
        Objects.requireNonNull(projectId, "projectId must not be null");
        return repository.findSiteIdsByProject(projectId);
    }

    @Transactional
    public void replaceProjectsForSite(UUID siteId, Collection<UUID> projectIds) {
        Objects.requireNonNull(siteId, "siteId must not be null");
        repository.replaceProjectsForSite(siteId, normalize(projectIds));
    }

    @Transactional
    public void replaceSitesForProject(UUID projectId, Collection<UUID> siteIds) {
        Objects.requireNonNull(projectId, "projectId must not be null");
        repository.replaceSitesForProject(projectId, normalize(siteIds));
    }

    private List<UUID> normalize(Collection<UUID> ids) {
        if (ids == null) return List.of();
        return ids.stream().filter(Objects::nonNull).distinct().toList();
    }
}
