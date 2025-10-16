package at.htlle.freq.domain;

import org.springframework.stereotype.Component;

@Component
public class CountryFactory {
    public Country create(String countryCode, String countryName) {
        return new Country(countryCode, countryName);
    }
}
