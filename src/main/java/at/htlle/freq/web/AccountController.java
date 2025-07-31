package at.htlle.freq.web;

import at.htlle.freq.application.AccountService;
import at.htlle.freq.domain.Account;
import at.htlle.freq.domain.SearchHit;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST-Endpunkte rund um Accounts + globale Lucene-Suche.
 */
@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /* ───────────────────────────────
     *        CRUD-Operationen
     * ─────────────────────────────── */

    @GetMapping
    public List<Account> findAll() {
        return accountService.getAllAccounts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> findById(@PathVariable int id) {
        Optional<Account> acc = accountService.getAccountById(id);
        return acc.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Account create(@RequestBody Account account) {
        return accountService.createAccount(account);
    }

    /* ───────────────────────────────
     *   L U C E N E  –  S U C H E
     * ─────────────────────────────── */

    /**
     * Globale Volltextsuche über **alle** Entitäten.
     * <ul>
     *   <li>Leerer oder fehlender <code>query</code> ⇒ <b>*:*</b> (alle Dokumente)</li>
     *   <li>Antwort ist immer 200 OK + (ggf. leere) Liste von {@link SearchHit}</li>
     * </ul>
     */
    @GetMapping("/lucene-search")
    public ResponseEntity<List<SearchHit>> luceneSearch(
            @RequestParam(required = false) String query) {

        if (query == null || query.isBlank()) {
            query = "*:*";                          // alle Dokumente
        }

        List<SearchHit> results = accountService.searchAccountsByName(query);
        return ResponseEntity.ok(results);
    }
}