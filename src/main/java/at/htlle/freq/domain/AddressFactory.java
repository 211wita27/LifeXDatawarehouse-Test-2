package at.htlle.freq.domain;

import org.springframework.stereotype.Component;

@Component
public class AddressFactory {
    public Address create(String street, String cityID) {
        return new Address(null, street, cityID);
    }
}
