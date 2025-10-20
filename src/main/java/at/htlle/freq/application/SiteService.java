// src/main/java/at/htlle/freq/application/SiteService.java
package at.htlle.freq.application;

import at.htlle.freq.domain.Site;
import at.htlle.freq.domain.SiteRepository;
import at.htlle.freq.infrastructure.lucene.LuceneIndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.*;

@Service
public class SiteService {

    private static final Logger log = LoggerFactory.getLogger(SiteService.class);

    private final SiteRepository repo;
    private final LuceneIndexService lucene;

    public SiteService(SiteRepository repo, LuceneIndexService lucene) {
        this.repo = repo;
        this.lucene = lucene;
    }

    // ---------- Queries ----------

    public List<Site> getAllSites() {
        return repo.findAll();
    }

    public Optional<Site> getSiteById(UUID id) {
        Objects.requireNonNull(id, "id must not be null");
        return repo.findById(id);
    }

    public List<Site> getSitesByProject(UUID projectId) {
        Objects.requireNonNull(projectId, "projectId must not be null");
        return repo.findByProject(projectId);
    }

    // ---------- Commands ----------

    @Transactional
    public Site createOrUpdateSite(Site incoming) {
        Objects.requireNonNull(incoming, "site payload must not be null");

        if (isBlank(incoming.getSiteName()))
            throw new IllegalArgumentException("SiteName is required");
        if (incoming.getProjectID() == null)
            throw new IllegalArgumentException("ProjectID is required");

        Site saved = repo.save(incoming);
        registerAfterCommitIndexing(saved);

        log.info("Site gespeichert: id={} name='{}' projectID={}",
                saved.getSiteID(), saved.getSiteName(), saved.getProjectID());
        return saved;
    }

    @Transactional
    public Optional<Site> updateSite(UUID id, Site patch) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(patch, "patch must not be null");

        return repo.findById(id).map(existing -> {
            existing.setSiteName(nvl(patch.getSiteName(), existing.getSiteName()));
            existing.setProjectID(patch.getProjectID() != null ? patch.getProjectID() : existing.getProjectID());
            existing.setAddressID(patch.getAddressID() != null ? patch.getAddressID() : existing.getAddressID());
            existing.setFireZone(nvl(patch.getFireZone(), existing.getFireZone()));
            existing.setTenantCount(patch.getTenantCount() != null ? patch.getTenantCount() : existing.getTenantCount());

            Site saved = repo.save(existing);
            registerAfterCommitIndexing(saved);

            log.info("Site aktualisiert: id={} name='{}'", id, saved.getSiteName());
            return saved;
        });
    }

    @Transactional
    public void deleteSite(UUID id) {
        Objects.requireNonNull(id, "id must not be null");
        repo.findById(id).ifPresent(s -> {
            log.info("Site gelöscht: id={} name='{}'", id, s.getSiteName());
            // Optional: lucene.deleteSite(id.toString());
        });
    }

    // ---------- Internals ----------

    private void registerAfterCommitIndexing(Site s) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            indexToLucene(s);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                indexToLucene(s);
            }
        });
    }

    private void indexToLucene(Site s) {
        try {
            lucene.indexSite(
                    s.getSiteID() != null ? s.getSiteID().toString() : null,
                    s.getProjectID() != null ? s.getProjectID().toString() : null,
                    s.getAddressID() != null ? s.getAddressID().toString() : null,
                    s.getSiteName(),
                    s.getFireZone(),
                    s.getTenantCount()
            );
            log.debug("Site in Lucene indexiert: id={}", s.getSiteID());
        } catch (Exception e) {
            log.error("Lucene-Indexing für Site {} fehlgeschlagen", s.getSiteID(), e);
        }
    }

    // ---------- Utils ----------

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String nvl(String in, String fallback) {
        return in != null ? in : fallback;
    }
}
