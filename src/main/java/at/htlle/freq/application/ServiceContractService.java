// src/main/java/at/htlle/freq/application/ServiceContractService.java
package at.htlle.freq.application;

import at.htlle.freq.domain.ServiceContract;
import at.htlle.freq.domain.ServiceContractRepository;
import at.htlle.freq.infrastructure.lucene.LuceneIndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.*;

@Service
public class ServiceContractService {

    private static final Logger log = LoggerFactory.getLogger(ServiceContractService.class);

    private final ServiceContractRepository repo;
    private final LuceneIndexService lucene;

    public ServiceContractService(ServiceContractRepository repo, LuceneIndexService lucene) {
        this.repo = repo;
        this.lucene = lucene;
    }

    // ---------- Queries ----------

    public List<ServiceContract> getAllContracts() {
        return repo.findAll();
    }

    public Optional<ServiceContract> getContractById(UUID id) {
        Objects.requireNonNull(id, "id must not be null");
        return repo.findById(id);
    }

    public List<ServiceContract> getContractsByAccount(UUID accountId) {
        Objects.requireNonNull(accountId, "accountId must not be null");
        return repo.findByAccount(accountId);
    }

    // ---------- Commands ----------

    @Transactional
    public ServiceContract createOrUpdateContract(ServiceContract incoming) {
        Objects.requireNonNull(incoming, "contract payload must not be null");

        if (incoming.getAccountID() == null)
            throw new IllegalArgumentException("AccountID is required");
        if (incoming.getProjectID() == null)
            throw new IllegalArgumentException("ProjectID is required");
        if (isBlank(incoming.getContractNumber()))
            throw new IllegalArgumentException("ContractNumber is required");
        if (isBlank(incoming.getStatus()))
            throw new IllegalArgumentException("Status is required");

        ServiceContract saved = repo.save(incoming);
        registerAfterCommitIndexing(saved);

        log.info("ServiceContract gespeichert: id={} contractNumber='{}' status='{}'",
                saved.getContractID(), saved.getContractNumber(), saved.getStatus());
        return saved;
    }

    @Transactional
    public Optional<ServiceContract> updateContract(UUID id, ServiceContract patch) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(patch, "patch must not be null");

        return repo.findById(id).map(existing -> {
            existing.setAccountID(patch.getAccountID() != null ? patch.getAccountID() : existing.getAccountID());
            existing.setProjectID(patch.getProjectID() != null ? patch.getProjectID() : existing.getProjectID());
            existing.setSiteID(patch.getSiteID() != null ? patch.getSiteID() : existing.getSiteID());
            existing.setContractNumber(nvl(patch.getContractNumber(), existing.getContractNumber()));
            existing.setStatus(nvl(patch.getStatus(), existing.getStatus()));
            existing.setStartDate(nvl(patch.getStartDate(), existing.getStartDate()));
            existing.setEndDate(nvl(patch.getEndDate(), existing.getEndDate()));

            ServiceContract saved = repo.save(existing);
            registerAfterCommitIndexing(saved);

            log.info("ServiceContract aktualisiert: id={} number='{}' status='{}'",
                    id, saved.getContractNumber(), saved.getStatus());
            return saved;
        });
    }

    @Transactional
    public void deleteContract(UUID id) {
        Objects.requireNonNull(id, "id must not be null");
        repo.findById(id).ifPresent(sc -> {
            log.info("ServiceContract gelöscht: id={} contractNumber='{}'", id, sc.getContractNumber());
            // Optional: lucene.deleteServiceContract(id.toString());
        });
    }

    // ---------- Internals ----------

    private void registerAfterCommitIndexing(ServiceContract sc) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            indexToLucene(sc);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                indexToLucene(sc);
            }
        });
    }

    private void indexToLucene(ServiceContract sc) {
        try {
            lucene.indexServiceContract(
                    sc.getContractID() != null ? sc.getContractID().toString() : null,
                    sc.getAccountID() != null ? sc.getAccountID().toString() : null,
                    sc.getProjectID() != null ? sc.getProjectID().toString() : null,
                    sc.getSiteID() != null ? sc.getSiteID().toString() : null,
                    sc.getContractNumber(),
                    sc.getStatus(),
                    sc.getStartDate(),
                    sc.getEndDate()
            );
            log.debug("ServiceContract in Lucene indexiert: id={}", sc.getContractID());
        } catch (Exception e) {
            log.error("Lucene-Indexing für ServiceContract {} fehlgeschlagen", sc.getContractID(), e);
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
