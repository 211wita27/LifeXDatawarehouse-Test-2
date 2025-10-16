// src/main/java/at/htlle/freq/application/AccountService.java
package at.htlle.freq.application;

import at.htlle.freq.domain.Account;
import at.htlle.freq.domain.AccountRepository;
import at.htlle.freq.infrastructure.lucene.LuceneIndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository repo;
    private final LuceneIndexService lucene;

    public AccountService(AccountRepository repo, LuceneIndexService lucene) {
        this.repo = repo;
        this.lucene = lucene;
    }

    // ---------- Queries ----------

    public List<Account> getAllAccounts() {
        return repo.findAll();
    }

    public Optional<Account> getAccountById(UUID id) {
        Objects.requireNonNull(id, "id must not be null");
        return repo.findById(id);
    }

    public Optional<Account> getAccountByName(String name) {
        if (isBlank(name)) return Optional.empty();
        return repo.findByName(name.trim());
    }

    // ---------- Commands ----------

    /**
     * Legt einen Account an (oder updated, falls ID gesetzt ist) und indexiert ihn
     * in Lucene NACH erfolgreichem Commit der DB-Transaktion.
     */
    @Transactional
    public Account createAccount(Account incoming) {
        Objects.requireNonNull(incoming, "account payload must not be null");

        // einfache Validierung
        if (isBlank(incoming.getAccountName())) {
            throw new IllegalArgumentException("AccountName is required");
        }

        // Persistieren (Repo generiert UUID, falls null)
        Account saved = repo.save(incoming);
        UUID id = saved.getAccountID();

        // Nach Commit indexieren, damit Index & DB konsistent bleiben
        registerAfterCommitIndexing(saved);

        log.info("Account gespeichert: id={} name='{}'", id, saved.getAccountName());
        return saved;
    }

    /**
     * Optionales Update (nicht vom Controller verlangt, aber nützlich).
     */
    @Transactional
    public Optional<Account> updateAccount(UUID id, Account patch) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(patch, "patch must not be null");

        return repo.findById(id).map(existing -> {
            // Felder überschreiben (einfaches Replace – bei Bedarf auf Patch-Logik ändern)
            existing.setAccountName(nvl(patch.getAccountName(), existing.getAccountName()));
            existing.setContactName(nvl(patch.getContactName(), existing.getContactName()));
            existing.setContactEmail(nvl(patch.getContactEmail(), existing.getContactEmail()));
            existing.setContactPhone(nvl(patch.getContactPhone(), existing.getContactPhone()));
            existing.setVatNumber(nvl(patch.getVatNumber(), existing.getVatNumber()));
            existing.setCountry(nvl(patch.getCountry(), existing.getCountry()));

            Account saved = repo.save(existing);
            registerAfterCommitIndexing(saved);
            log.info("Account aktualisiert: id={} name='{}'", id, saved.getAccountName());
            return saved;
        });
    }

    @Transactional
    public void deleteAccount(UUID id) {
        Objects.requireNonNull(id, "id must not be null");
        repo.deleteById(id);
        // Optional: Den Eintrag aus Lucene entfernen (hier simpel: reindexAll oder spezielles delete)
        // Wenn du in Lucene auch löschen willst, füge in deinem LuceneIndexService eine delete(id, type) Methode hinzu
        log.info("Account gelöscht: id={}", id);
    }

    // ---------- Internals ----------

    private void registerAfterCommitIndexing(Account a) {
        // Falls keine offene TX vorhanden ist, sofort indexieren (z. B. Tests)
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            indexToLucene(a);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                indexToLucene(a);
            }
        });
    }

    private void indexToLucene(Account a) {
        try {
            lucene.indexAccount(
                    a.getAccountID() != null ? a.getAccountID().toString() : null,
                    a.getAccountName(),
                    a.getCountry(),
                    a.getContactEmail()
            );
            log.debug("Account in Lucene indexiert: id={}", a.getAccountID());
        } catch (Exception e) {
            // Index-Fehler sollen die DB-Transaktion nicht rückgängig machen
            log.error("Lucene-Indexing für Account {} fehlgeschlagen", a.getAccountID(), e);
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String nvl(String in, String fallback) {
        return in != null ? in : fallback;
    }
}
