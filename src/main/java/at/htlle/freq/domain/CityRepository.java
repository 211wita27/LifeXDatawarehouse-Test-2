package at.htlle.freq.domain;

import java.util.List;
import java.util.Optional;

public interface CityRepository {
    Optional<City> findById(String id);
    List<City> findByCountry(String countryCode);
    void save(City city);
    List<City> findAll();
}
