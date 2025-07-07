package at.htlle.freq.web;

import at.htlle.freq.application.AccountService;
import at.htlle.freq.domain.Account;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        try {
            Account created = accountService.createAccount(name);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/by-name/{name}")
    public ResponseEntity<?> updateByName(@PathVariable String name, @RequestBody Map<String, String> body) {
        String newName = body.get("name");
        try {
            accountService.updateAccountByName(name, newName);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Account> search(@RequestParam(required = false) String name,
                                          @RequestParam(required = false) UUID id) {
        Optional<Account> result = accountService.searchAccount(name, id);
        return result.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/lucene-search")
    public ResponseEntity<List<Account>> luceneSearch(@RequestParam String query) {
        List<Account> results = accountService.searchAccountsByName(query);
        if (results.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(results);
    }
    @GetMapping
    public ResponseEntity<List<Account>> findAll() {
        return ResponseEntity.ok(accountService.findAllAccounts());
    }

}
