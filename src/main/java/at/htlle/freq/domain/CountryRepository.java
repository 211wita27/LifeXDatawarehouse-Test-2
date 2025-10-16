package at.htlle.freq.domain;

import java.util.List;
import java.util.Optional;

public interface CountryRepository {
    Optional<Country> findById(String code);
    void save(Country country);
    List<Country> findAll();
}
