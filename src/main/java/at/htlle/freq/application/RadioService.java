// src/main/java/at/htlle/freq/application/RadioService.java
package at.htlle.freq.application;

import at.htlle.freq.domain.Radio;
import at.htlle.freq.domain.RadioRepository;
import at.htlle.freq.infrastructure.lucene.LuceneIndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.*;

@Service
public class RadioService {

    private static final Logger log = LoggerFactory.getLogger(RadioService.class);

    private final RadioRepository repo;
    private final LuceneIndexService lucene;

    public RadioService(RadioRepository repo, LuceneIndexService lucene) {
        this.repo = repo;
        this.lucene = lucene;
    }

    // ---------- Queries ----------

    public List<Radio> getAllRadios() {
        return repo.findAll();
    }

    public Optional<Radio> getRadioById(UUID id) {
        Objects.requireNonNull(id, "id must not be null");
        return repo.findById(id);
    }

    public List<Radio> getRadiosBySite(UUID siteId) {
        Objects.requireNonNull(siteId, "siteId must not be null");
        return repo.findBySite(siteId);
    }

    // ---------- Commands ----------

    @Transactional
    public Radio createOrUpdateRadio(Radio incoming) {
        Objects.requireNonNull(incoming, "radio payload must not be null");

        if (incoming.getSiteID() == null)
            throw new IllegalArgumentException("SiteID is required");
        if (isBlank(incoming.getRadioBrand()))
            throw new IllegalArgumentException("RadioBrand is required");
        if (isBlank(incoming.getRadioSerialNr()))
            throw new IllegalArgumentException("RadioSerialNr is required");
        if (isBlank(incoming.getMode()))
            throw new IllegalArgumentException("Mode is required");

        Radio saved = repo.save(incoming);
        registerAfterCommitIndexing(saved);

        log.info("Radio gespeichert: id={} site={} brand='{}' serialNr='{}'",
                saved.getRadioID(), saved.getSiteID(), saved.getRadioBrand(), saved.getRadioSerialNr());
        return saved;
    }

    @Transactional
    public Optional<Radio> updateRadio(UUID id, Radio patch) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(patch, "patch must not be null");

        return repo.findById(id).map(existing -> {
            existing.setSiteID(patch.getSiteID() != null ? patch.getSiteID() : existing.getSiteID());
            existing.setAssignedClientID(patch.getAssignedClientID() != null ? patch.getAssignedClientID() : existing.getAssignedClientID());
            existing.setRadioBrand(nvl(patch.getRadioBrand(), existing.getRadioBrand()));
            existing.setRadioSerialNr(nvl(patch.getRadioSerialNr(), existing.getRadioSerialNr()));
            existing.setMode(nvl(patch.getMode(), existing.getMode()));
            existing.setDigitalStandard(nvl(patch.getDigitalStandard(), existing.getDigitalStandard()));

            Radio saved = repo.save(existing);
            registerAfterCommitIndexing(saved);

            log.info("Radio aktualisiert: id={} brand='{}'", id, saved.getRadioBrand());
            return saved;
        });
    }

    @Transactional
    public void deleteRadio(UUID id) {
        Objects.requireNonNull(id, "id must not be null");
        repo.findById(id).ifPresent(r -> {
            log.info("Radio gelöscht: id={} brand='{}' serialNr='{}'",
                    id, r.getRadioBrand(), r.getRadioSerialNr());
            // Optional: lucene.deleteRadio(id.toString());
        });
    }

    // ---------- Internals ----------

    private void registerAfterCommitIndexing(Radio r) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            indexToLucene(r);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                indexToLucene(r);
            }
        });
    }

    private void indexToLucene(Radio r) {
        try {
            lucene.indexRadio(
                    r.getRadioID() != null ? r.getRadioID().toString() : null,
                    r.getSiteID() != null ? r.getSiteID().toString() : null,
                    r.getAssignedClientID() != null ? r.getAssignedClientID().toString() : null,
                    r.getRadioBrand(),
                    r.getRadioSerialNr(),
                    r.getMode(),
                    r.getDigitalStandard()
            );
            log.debug("Radio in Lucene indexiert: id={}", r.getRadioID());
        } catch (Exception e) {
            log.error("Lucene-Indexing für Radio {} fehlgeschlagen", r.getRadioID(), e);
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
