package at.htlle.freq.domain;

import java.util.*;

public interface SiteRepository {
    Optional<Site> findById(UUID id);
    List<Site> findByProject(UUID projectId);
    Site save(Site site);
    List<Site> findAll();
}
