package at.htlle.freq.domain;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AccountFactory {
    public Account create(String name) {
        return new Account(UUID.randomUUID(), name);
    }
}
