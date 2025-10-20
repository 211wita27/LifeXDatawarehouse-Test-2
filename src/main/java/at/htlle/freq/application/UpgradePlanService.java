// src/main/java/at/htlle/freq/application/UpgradePlanService.java
package at.htlle.freq.application;

import at.htlle.freq.domain.UpgradePlan;
import at.htlle.freq.domain.UpgradePlanRepository;
import at.htlle.freq.infrastructure.lucene.LuceneIndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.*;

@Service
public class UpgradePlanService {

    private static final Logger log = LoggerFactory.getLogger(UpgradePlanService.class);

    private final UpgradePlanRepository repo;
    private final LuceneIndexService lucene;

    public UpgradePlanService(UpgradePlanRepository repo, LuceneIndexService lucene) {
        this.repo = repo;
        this.lucene = lucene;
    }

    // ---------- Queries ----------

    public List<UpgradePlan> getAllUpgradePlans() {
        return repo.findAll();
    }

    public Optional<UpgradePlan> getUpgradePlanById(UUID id) {
        Objects.requireNonNull(id, "id must not be null");
        return repo.findById(id);
    }

    public List<UpgradePlan> getUpgradePlansBySite(UUID siteId) {
        Objects.requireNonNull(siteId, "siteId must not be null");
        return repo.findBySite(siteId);
    }

    // ---------- Commands ----------

    @Transactional
    public UpgradePlan createOrUpdateUpgradePlan(UpgradePlan incoming) {
        Objects.requireNonNull(incoming, "upgrade plan payload must not be null");

        if (incoming.getSiteID() == null)
            throw new IllegalArgumentException("SiteID is required");
        if (incoming.getSoftwareID() == null)
            throw new IllegalArgumentException("SoftwareID is required");
        if (isBlank(incoming.getStatus()))
            throw new IllegalArgumentException("Status is required");

        UpgradePlan saved = repo.save(incoming);
        registerAfterCommitIndexing(saved);

        log.info("UpgradePlan gespeichert: id={} site={} software={} status='{}'",
                saved.getUpgradePlanID(), saved.getSiteID(), saved.getSoftwareID(), saved.getStatus());
        return saved;
    }

    @Transactional
    public Optional<UpgradePlan> updateUpgradePlan(UUID id, UpgradePlan patch) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(patch, "patch must not be null");

        return repo.findById(id).map(existing -> {
            existing.setSiteID(patch.getSiteID() != null ? patch.getSiteID() : existing.getSiteID());
            existing.setSoftwareID(patch.getSoftwareID() != null ? patch.getSoftwareID() : existing.getSoftwareID());
            existing.setPlannedWindowStart(nvl(patch.getPlannedWindowStart(), existing.getPlannedWindowStart()));
            existing.setPlannedWindowEnd(nvl(patch.getPlannedWindowEnd(), existing.getPlannedWindowEnd()));
            existing.setStatus(nvl(patch.getStatus(), existing.getStatus()));
            existing.setCreatedAt(nvl(patch.getCreatedAt(), existing.getCreatedAt()));
            existing.setCreatedBy(nvl(patch.getCreatedBy(), existing.getCreatedBy()));

            UpgradePlan saved = repo.save(existing);
            registerAfterCommitIndexing(saved);

            log.info("UpgradePlan aktualisiert: id={} status='{}'", id, saved.getStatus());
            return saved;
        });
    }

    @Transactional
    public void deleteUpgradePlan(UUID id) {
        Objects.requireNonNull(id, "id must not be null");
        repo.findById(id).ifPresent(up -> {
            log.info("UpgradePlan gelöscht: id={} site={} software={}",
                    id, up.getSiteID(), up.getSoftwareID());
            // Optional: lucene.deleteUpgradePlan(id.toString());
        });
    }

    // ---------- Internals ----------

    private void registerAfterCommitIndexing(UpgradePlan up) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            indexToLucene(up);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                indexToLucene(up);
            }
        });
    }

    private void indexToLucene(UpgradePlan up) {
        try {
            lucene.indexUpgradePlan(
                    up.getUpgradePlanID() != null ? up.getUpgradePlanID().toString() : null,
                    up.getSiteID() != null ? up.getSiteID().toString() : null,
                    up.getSoftwareID() != null ? up.getSoftwareID().toString() : null,
                    up.getPlannedWindowStart(),
                    up.getPlannedWindowEnd(),
                    up.getStatus(),
                    up.getCreatedAt(),
                    up.getCreatedBy()
            );
            log.debug("UpgradePlan in Lucene indexiert: id={}", up.getUpgradePlanID());
        } catch (Exception e) {
            log.error("Lucene-Indexing für UpgradePlan {} fehlgeschlagen", up.getUpgradePlanID(), e);
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
