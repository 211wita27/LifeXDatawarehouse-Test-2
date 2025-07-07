package at.htlle.freq.web;

import at.htlle.freq.application.AccountService;
import at.htlle.freq.domain.Account;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST-Endpunkte rund um Accounts + Lucene-Suche.
 */
@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /* ────────────────────────────────
     * CRUD-Operationen
     * ──────────────────────────────── */

    @GetMapping
    public List<Account> findAll() {
        return accountService.getAllAccounts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> findById(@PathVariable UUID id) {
        return accountService.getAccountById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Account create(@RequestBody Account account) {
        return accountService.createAccount(account);
    }

    /* ────────────────────────────────
     * Lucene-Volltextsuche
     * ──────────────────────────────── */

    /**
     * Sucht Accounts per Lucene nach ihrem Namen.
     * <p>
     * - Leere oder fehlende <code>query</code> ⇒ <b>*:*</b> (alle Dokumente).<br>
     * - Antwort ist immer <b>200 OK</b> und enthält <i>immer</i> eine (ggf. leere) Liste,
     *   sodass das Front-End gefahrlos <code>response.json()</code> aufrufen kann.
     */
    @GetMapping("/lucene-search")
    public ResponseEntity<List<Account>> luceneSearch(@RequestParam(required = false) String query) {

        /*  ▼▼▼  Änderung gegenüber vorher  ▼▼▼  */
        if (query == null || query.isBlank()) {
            query = "*:*";                    // leeres Feld ⇒ alle Accounts anzeigen
        }
        /*  ▲▲▲  -------------------------  ▲▲▲  */

        List<Account> results = accountService.searchAccountsByName(query);
        return ResponseEntity.ok(results);     // kein 204 No Content mehr
    }
}