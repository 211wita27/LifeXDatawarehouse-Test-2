package at.htlle.freq.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {
    Optional<Account> findById(UUID id);
    Optional<Account> findByName(String name);
    void save(Account account);
    List<Account> findAll();

}
