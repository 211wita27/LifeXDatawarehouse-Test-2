// at/htlle/freq/domain/AccountRepository.java (oder dein Repo-Package)
package at.htlle.freq.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {
    Optional<Account> findById(UUID id);
    Optional<Account> findByName(String name);
    Account save(Account account);
    List<Account> findAll();
    void deleteById(UUID id);   // <— ergänzen
}
