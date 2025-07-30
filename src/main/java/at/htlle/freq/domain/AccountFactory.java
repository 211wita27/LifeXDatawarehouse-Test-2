package at.htlle.freq.domain;

import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.UUID;

@Component
public class AccountFactory {
    Random random = new Random();
    public Account create(String AccountName,String ContactEmail,String ContactPhone,String VATNumber,String Country) {
        return new Account(random.nextInt(), AccountName, ContactEmail, ContactPhone, VATNumber, Country);
    }
}
