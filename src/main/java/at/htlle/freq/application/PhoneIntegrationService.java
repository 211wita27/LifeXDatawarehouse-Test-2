// src/main/java/at/htlle/freq/application/PhoneIntegrationService.java
package at.htlle.freq.application;

import at.htlle.freq.domain.PhoneIntegration;
import at.htlle.freq.domain.PhoneIntegrationRepository;
import at.htlle.freq.infrastructure.lucene.LuceneIndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.*;

@Service
public class PhoneIntegrationService {

    private static final Logger log = LoggerFactory.getLogger(PhoneIntegrationService.class);

    private final PhoneIntegrationRepository repo;
    private final LuceneIndexService lucene;

    public PhoneIntegrationService(PhoneIntegrationRepository repo, LuceneIndexService lucene) {
        this.repo = repo;
        this.lucene = lucene;
    }

    // ---------- Queries ----------

    public List<PhoneIntegration> getAllPhoneIntegrations() {
        return repo.findAll();
    }

    public Optional<PhoneIntegration> getPhoneIntegrationById(UUID id) {
        Objects.requireNonNull(id, "id must not be null");
        return repo.findById(id);
    }

    public List<PhoneIntegration> getPhoneIntegrationsByClient(UUID clientId) {
        Objects.requireNonNull(clientId, "clientId must not be null");
        return repo.findByClient(clientId);
    }

    // ---------- Commands ----------

    @Transactional
    public PhoneIntegration createOrUpdatePhoneIntegration(PhoneIntegration incoming) {
        Objects.requireNonNull(incoming, "phoneIntegration payload must not be null");

        if (incoming.getClientID() == null)
            throw new IllegalArgumentException("ClientID is required");
        if (isBlank(incoming.getPhoneType()))
            throw new IllegalArgumentException("PhoneType is required");

        PhoneIntegration saved = repo.save(incoming);
        registerAfterCommitIndexing(saved);

        log.info("PhoneIntegration gespeichert: id={} client={} type='{}'",
                saved.getPhoneIntegrationID(), saved.getClientID(), saved.getPhoneType());
        return saved;
    }

    @Transactional
    public Optional<PhoneIntegration> updatePhoneIntegration(UUID id, PhoneIntegration patch) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(patch, "patch must not be null");

        return repo.findById(id).map(existing -> {
            existing.setClientID(patch.getClientID() != null ? patch.getClientID() : existing.getClientID());
            existing.setPhoneType(nvl(patch.getPhoneType(), existing.getPhoneType()));
            existing.setPhoneBrand(nvl(patch.getPhoneBrand(), existing.getPhoneBrand()));
            existing.setPhoneSerialNr(nvl(patch.getPhoneSerialNr(), existing.getPhoneSerialNr()));
            existing.setPhoneFirmware(nvl(patch.getPhoneFirmware(), existing.getPhoneFirmware()));

            PhoneIntegration saved = repo.save(existing);
            registerAfterCommitIndexing(saved);

            log.info("PhoneIntegration aktualisiert: id={} client={} type='{}'",
                    id, saved.getClientID(), saved.getPhoneType());
            return saved;
        });
    }

    @Transactional
    public void deletePhoneIntegration(UUID id) {
        Objects.requireNonNull(id, "id must not be null");
        repo.findById(id).ifPresent(p -> {
            log.info("PhoneIntegration gelöscht: id={} client={} type='{}'",
                    id, p.getClientID(), p.getPhoneType());
            // Optional: lucene.deletePhoneIntegration(id.toString());
        });
    }

    // ---------- Internals ----------

    private void registerAfterCommitIndexing(PhoneIntegration p) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            indexToLucene(p);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                indexToLucene(p);
            }
        });
    }

    private void indexToLucene(PhoneIntegration p) {
        try {
            lucene.indexPhoneIntegration(
                    p.getPhoneIntegrationID() != null ? p.getPhoneIntegrationID().toString() : null,
                    p.getClientID() != null ? p.getClientID().toString() : null,
                    p.getPhoneType(),
                    p.getPhoneBrand(),
                    p.getPhoneSerialNr(),
                    p.getPhoneFirmware()
            );
            log.debug("PhoneIntegration in Lucene indexiert: id={}", p.getPhoneIntegrationID());
        } catch (Exception e) {
            log.error("Lucene-Indexing für PhoneIntegration {} fehlgeschlagen", p.getPhoneIntegrationID(), e);
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
