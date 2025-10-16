package at.htlle.freq.domain;

import org.springframework.stereotype.Component;

@Component
public class CityFactory {
    public City create(String cityID, String cityName, String countryCode) {
        return new City(cityID, cityName, countryCode);
    }
}
