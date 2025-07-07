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
@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountFactory accountFactory;
    private final ProducerTemplate camel;
    private final LuceneIndexService luceneIndexService;

    public AccountService(AccountRepository accountRepository, AccountFactory accountFactory, ProducerTemplate camel, LuceneIndexService luceneIndexService) {
        this.accountRepository = accountRepository;
        this.accountFactory = accountFactory;
        this.camel = camel;
        this.luceneIndexService = luceneIndexService;
    }

    public Account createAccount(String name) {
        if (accountRepository.findByName(name).isPresent()) {
            throw new IllegalArgumentException("Account with name already exists: " + name);
        }
        Account account = accountFactory.create(name);
        accountRepository.save(account);
        camel.sendBody("direct:index-account", account);
        return account; // <- HINZUGEFÜGT!
    }


    public void updateAccount(String oldName, String newName) {
        Account account = accountRepository.findByName(oldName)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + oldName));
        if (accountRepository.findByName(newName).isPresent()) {
            throw new IllegalArgumentException("New name already in use: " + newName);
        }
        account.setName(newName);
        accountRepository.save(account);
        camel.sendBody("direct:index-account", account);
    }

    public void updateAccountByName(String oldName, String newName) {
        updateAccount(oldName, newName); // Verwende vorhandene Logik
    }
    public Optional<Account> searchAccount(String name, UUID id) {
        if (id != null) {
            return accountRepository.findById(id);
        } else if (name != null) {
            return accountRepository.findByName(name);
        }
        return Optional.empty();
    }
    public List<Account> searchAccountsByName(String query) {
        return luceneIndexService.search(query);
    }
    public List<Account> findAllAccounts() {
        return accountRepository.findAll();
    }


}
