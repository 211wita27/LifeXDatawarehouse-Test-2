// src/main/java/at/htlle/freq/application/DeploymentVariantService.java
package at.htlle.freq.application;

import at.htlle.freq.domain.DeploymentVariant;
import at.htlle.freq.domain.DeploymentVariantRepository;
import at.htlle.freq.infrastructure.lucene.LuceneIndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.*;

@Service
public class DeploymentVariantService {

    private static final Logger log = LoggerFactory.getLogger(DeploymentVariantService.class);

    private final DeploymentVariantRepository repo;
    private final LuceneIndexService lucene;

    public DeploymentVariantService(DeploymentVariantRepository repo, LuceneIndexService lucene) {
        this.repo = repo;
        this.lucene = lucene;
    }

    // ---------- Queries ----------

    public List<DeploymentVariant> getAllVariants() {
        return repo.findAll();
    }

    public Optional<DeploymentVariant> getVariantById(UUID id) {
        Objects.requireNonNull(id, "variant id must not be null");
        return repo.findById(id);
    }

    public Optional<DeploymentVariant> getVariantByCode(String code) {
        if (isBlank(code)) return Optional.empty();
        return repo.findByCode(code.trim());
    }

    public Optional<DeploymentVariant> getVariantByName(String name) {
        if (isBlank(name)) return Optional.empty();
        return repo.findByName(name.trim());
    }

    // ---------- Commands ----------

    /**
     * Legt eine DeploymentVariant an oder aktualisiert sie
     * und indexiert sie in Lucene NACH erfolgreichem Commit.
     */
    @Transactional
    public DeploymentVariant createOrUpdateVariant(DeploymentVariant incoming) {
        Objects.requireNonNull(incoming, "variant payload must not be null");

        if (isBlank(incoming.getVariantCode()))
            throw new IllegalArgumentException("VariantCode is required");
        if (isBlank(incoming.getVariantName()))
            throw new IllegalArgumentException("VariantName is required");

        DeploymentVariant saved = repo.save(incoming);
        registerAfterCommitIndexing(saved);

        log.info("DeploymentVariant gespeichert: id={} code='{}' name='{}'",
                saved.getVariantID(), saved.getVariantCode(), saved.getVariantName());
        return saved;
    }

    @Transactional
    public Optional<DeploymentVariant> updateVariant(UUID id, DeploymentVariant patch) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(patch, "patch must not be null");

        return repo.findById(id).map(existing -> {
            existing.setVariantCode(nvl(patch.getVariantCode(), existing.getVariantCode()));
            existing.setVariantName(nvl(patch.getVariantName(), existing.getVariantName()));
            existing.setDescription(nvl(patch.getDescription(), existing.getDescription()));
            existing.setActive(patch.isActive());

            DeploymentVariant saved = repo.save(existing);
            registerAfterCommitIndexing(saved);

            log.info("DeploymentVariant aktualisiert: id={} name='{}'",
                    id, saved.getVariantName());
            return saved;
        });
    }

    @Transactional
    public void deleteVariant(UUID id) {
        Objects.requireNonNull(id, "id must not be null");
        repo.findById(id).ifPresent(v -> {
            log.info("DeploymentVariant gelöscht: id={} name='{}'",
                    id, v.getVariantName());
            // Optional: lucene.deleteVariant(id.toString());
        });
    }

    // ---------- Internals ----------

    private void registerAfterCommitIndexing(DeploymentVariant v) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            indexToLucene(v);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                indexToLucene(v);
            }
        });
    }

    private void indexToLucene(DeploymentVariant v) {
        try {
            lucene.indexDeploymentVariant(
                    v.getVariantID() != null ? v.getVariantID().toString() : null,
                    v.getVariantCode(),
                    v.getVariantName(),
                    v.getDescription(),
                    Boolean.parseBoolean(Boolean.toString(v.isActive()))
            );
            log.debug("DeploymentVariant in Lucene indexiert: id={}", v.getVariantID());
        } catch (Exception e) {
            log.error("Lucene-Indexing für DeploymentVariant {} fehlgeschlagen", v.getVariantID(), e);
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
