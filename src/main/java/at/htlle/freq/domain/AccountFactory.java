package at.htlle.freq.domain;

import org.springframework.stereotype.Component;

/**
 * Erzeugt Accounts ohne manuelle IDs – die DB vergibt die ID (AUTO_INCREMENT).
 */
@Component
public class AccountFactory {

    public Account create(String accountName,
                          String contactEmail,
                          String contactPhone,
                          String vatNumber,
                          String country) {
        // AccountID = 0 -> Repository macht INSERT und liest die generierte ID zurück
        return new Account(
                0,
                accountName,
                contactEmail,
                contactPhone,
                vatNumber,
                country
        );
    }
}