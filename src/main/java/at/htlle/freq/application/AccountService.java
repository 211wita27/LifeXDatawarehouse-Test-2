package at.htlle.freq.application;

import at.htlle.freq.domain.Account;
import at.htlle.freq.domain.AccountFactory;
import at.htlle.freq.domain.AccountRepository;
import at.htlle.freq.infrastructure.lucene.LuceneIndexService;
import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Zentrale Geschäftslogik rund um Accounts.
 * <p>
 * Enthält sowohl CRUD-Operationen (persistiert via {@link AccountRepository})
 * als auch die Delegation an den Lucene-Index für Volltextsuche.
 */
@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountFactory accountFactory;
    private final ProducerTemplate camel;
    private final LuceneIndexService luceneIndexService;

    public AccountService(AccountRepository accountRepository,
                          AccountFactory accountFactory,
                          ProducerTemplate camel,
                          LuceneIndexService luceneIndexService) {
        this.accountRepository = accountRepository;
        this.accountFactory = accountFactory;
        this.camel = camel;
        this.luceneIndexService = luceneIndexService;
    }

    /* ────────────────────────────────
     *  CRUD-Operationen
     * ──────────────────────────────── */

    /**
     * Erstellt einen neuen Account aus dem übergebenen Namen.
     * <p>
     * Wirft eine {@link IllegalArgumentException}, falls der Name bereits existiert.
     */
    public Account createAccount(String AccountName,String ContactEmail,String ContactPhone,String VATNumber,String Country) {
        if (accountRepository.findByName(AccountName).isPresent()) {
            throw new IllegalArgumentException("Account with name already exists: " + AccountName);
        }
        Account account = accountFactory.create(AccountName,ContactEmail,ContactPhone,VATNumber,Country);
        accountRepository.save(account);
        // 👉 nach dem Insert sofort Lucene-Re-Index starten
        camel.sendBody("direct:index-account", account);
        return account;
    }

    /**
     * Überladene Variante, die bereits ein Account-Objekt erhält
     * (z. B. wenn es per JSON in den Controller kam).
     */
    public Account createAccount(Account account) {
        return createAccount(account.getAccountName(),account.getContactEmail(),account.getContactPhone(), account.getVATNumber(), account.getCountry());
    }

    /** Alle Accounts aus der Datenbank. */
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    /** Einzelnen Account per ID aus der Datenbank lesen. */
    public Optional<Account> getAccountById(int id) {
        return accountRepository.findById(id);
    }

    /**
     * Alias – wegen bestehender Tests noch belassen.
     * Intern einfach auf {@link #getAllAccounts()} gemappt.
     */
    public List<Account> findAllAccounts() {
        return getAllAccounts();
    }

    /* ────────────────────────────────
     *  Update- / Such-Logik
     * ──────────────────────────────── */

    public void updateAccount(String oldName, String newName) {
        Account account = accountRepository.findByName(oldName)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + oldName));

        if (accountRepository.findByName(newName).isPresent()) {
            throw new IllegalArgumentException("New name already in use: " + newName);
        }
        account.setAccountName(newName);
        accountRepository.save(account);
        camel.sendBody("direct:index-account", account);
    }

    public Optional<Account> searchAccount(String name, int id) {
       if(getAccountById(id) != null) {
           return getAccountById(id);
       }else if (name != null) {
           return accountRepository.findByName(name);
       }else{
           return Optional.empty();
       }
    }

    /** Delegiert an Lucene und liefert Trefferliste zurück. */
    public List<Account> searchAccountsByName(String query) {
        return luceneIndexService.search(query);
    }
}
