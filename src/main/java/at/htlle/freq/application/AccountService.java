package at.htlle.freq.application;

import at.htlle.freq.domain.*;
import at.htlle.freq.infrastructure.lucene.LuceneIndexService;
import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Geschäftslogik rund um Accounts.
 */
@Service
public class AccountService {

    private final AccountRepository   accountRepository;
    private final AccountFactory      accountFactory;
    private final ProducerTemplate    camel;
    private final LuceneIndexService  lucene;

    public AccountService(AccountRepository accountRepository,
                          AccountFactory accountFactory,
                          ProducerTemplate camel,
                          LuceneIndexService lucene) {
        this.accountRepository = accountRepository;
        this.accountFactory    = accountFactory;
        this.camel             = camel;
        this.lucene            = lucene;
    }

    /* ───────────────────────── CRUD ─────────────────────────────── */

    public Account createAccount(String name, String email, String phone,
                                 String vat, String country) {

        if (accountRepository.findByName(name).isPresent()) {
            throw new IllegalArgumentException("Account already exists: " + name);
        }

        Account acc = accountFactory.create(name, email, phone, vat, country);
        accountRepository.save(acc);

        // nach Insert → Einzelindex
        camel.sendBody("direct:index-account", acc);
        return acc;
    }

    /** Überladene Variante (kommt z. B. aus Controller-JSON). */
    public Account createAccount(Account acc) {
        return createAccount(acc.getAccountName(),
                acc.getContactEmail(),
                acc.getContactPhone(),
                acc.getVATNumber(),
                acc.getCountry());
    }

    public List<Account> getAllAccounts()      { return accountRepository.findAll(); }
    public Optional<Account> getAccountById(int id){ return accountRepository.findById(id); }

    /* ───────────────────────── Update / Suche ───────────────────── */

    public void updateAccount(String oldName, String newName) {
        Account acc = accountRepository.findByName(oldName)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + oldName));

        if (accountRepository.findByName(newName).isPresent()) {
            throw new IllegalArgumentException("Name already in use: " + newName);
        }
        acc.setAccountName(newName);
        accountRepository.save(acc);
        camel.sendBody("direct:index-account", acc);
    }

    public Optional<Account> searchAccount(String name, int id) {
        if (getAccountById(id).isPresent())            return getAccountById(id);
        if (name != null && !name.isBlank())           return accountRepository.findByName(name);
        return Optional.empty();
    }

    /* ───────────────────────── Lucene-Delegation ────────────────── */

    /**
     * Wird aktuell nur noch in Tests benötigt – liefert jetzt
     * die generischen {@link SearchHit}s zurück.
     */
    public List<SearchHit> searchAccountsByName(String query) {
        return lucene.search(query);
    }
}