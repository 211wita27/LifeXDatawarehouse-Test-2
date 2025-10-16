package at.htlle.freq.domain;

import org.springframework.stereotype.Component;

@Component
public class AccountFactory {
    public Account create(String accountName,
                          String contactName,
                          String contactEmail,
                          String contactPhone,
                          String vatNumber,
                          String country) {
        return new Account(
                null, // DB vergibt UUID
                accountName,
                contactName,
                contactEmail,
                contactPhone,
                vatNumber,
                country
        );
    }
}
