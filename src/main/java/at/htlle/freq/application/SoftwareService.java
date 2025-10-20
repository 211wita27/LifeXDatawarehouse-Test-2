// src/main/java/at/htlle/freq/application/SoftwareService.java
package at.htlle.freq.application;

import at.htlle.freq.domain.Software;
import at.htlle.freq.domain.SoftwareRepository;
import at.htlle.freq.infrastructure.lucene.LuceneIndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.*;

@Service
public class SoftwareService {

    private static final Logger log = LoggerFactory.getLogger(SoftwareService.class);

    private final SoftwareRepository repo;
    private final LuceneIndexService lucene;

    public SoftwareService(SoftwareRepository repo, LuceneIndexService lucene) {
        this.repo = repo;
        this.lucene = lucene;
    }

    // ---------- Queries ----------

    public List<Software> getAllSoftware() {
        return repo.findAll();
    }

    public Optional<Software> getSoftwareById(UUID id) {
        Objects.requireNonNull(id, "id must not be null");
        return repo.findById(id);
    }

    public List<Software> getSoftwareByName(String name) {
        if (isBlank(name)) return List.of();
        return repo.findByName(name.trim());
    }

    // ---------- Commands ----------

    @Transactional
    public Software createOrUpdateSoftware(Software incoming) {
        Objects.requireNonNull(incoming, "software payload must not be null");

        if (isBlank(incoming.getName()))
            throw new IllegalArgumentException("Name is required");
        if (isBlank(incoming.getRelease()))
            throw new IllegalArgumentException("Release is required");

        Software saved = repo.save(incoming);
        registerAfterCommitIndexing(saved);

        log.info("Software gespeichert: id={} name='{}' release='{}'",
                saved.getSoftwareID(), saved.getName(), saved.getRelease());
        return saved;
    }

    @Transactional
    public Optional<Software> updateSoftware(UUID id, Software patch) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(patch, "patch must not be null");

        return repo.findById(id).map(existing -> {
            existing.setName(nvl(patch.getName(), existing.getName()));
            existing.setRelease(nvl(patch.getRelease(), existing.getRelease()));
            existing.setRevision(nvl(patch.getRevision(), existing.getRevision()));
            existing.setSupportPhase(nvl(patch.getSupportPhase(), existing.getSupportPhase()));
            existing.setLicenseModel(nvl(patch.getLicenseModel(), existing.getLicenseModel()));
            existing.setEndOfSalesDate(nvl(patch.getEndOfSalesDate(), existing.getEndOfSalesDate()));
            existing.setSupportStartDate(nvl(patch.getSupportStartDate(), existing.getSupportStartDate()));
            existing.setSupportEndDate(nvl(patch.getSupportEndDate(), existing.getSupportEndDate()));

            Software saved = repo.save(existing);
            registerAfterCommitIndexing(saved);

            log.info("Software aktualisiert: id={} name='{}'", id, saved.getName());
            return saved;
        });
    }

    @Transactional
    public void deleteSoftware(UUID id) {
        Objects.requireNonNull(id, "id must not be null");
        repo.findById(id).ifPresent(sw -> {
            log.info("Software gelöscht: id={} name='{}' release='{}'",
                    id, sw.getName(), sw.getRelease());
            // Optional: lucene.deleteSoftware(id.toString());
        });
    }

    // ---------- Internals ----------

    private void registerAfterCommitIndexing(Software sw) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            indexToLucene(sw);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                indexToLucene(sw);
            }
        });
    }

    private void indexToLucene(Software sw) {
        try {
            lucene.indexSoftware(
                    sw.getSoftwareID() != null ? sw.getSoftwareID().toString() : null,
                    sw.getName(),
                    sw.getRelease(),
                    sw.getRevision(),
                    sw.getSupportPhase(),
                    sw.getLicenseModel(),
                    sw.getEndOfSalesDate(),
                    sw.getSupportStartDate(),
                    sw.getSupportEndDate()
            );
            log.debug("Software in Lucene indexiert: id={}", sw.getSoftwareID());
        } catch (Exception e) {
            log.error("Lucene-Indexing für Software {} fehlgeschlagen", sw.getSoftwareID(), e);
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
