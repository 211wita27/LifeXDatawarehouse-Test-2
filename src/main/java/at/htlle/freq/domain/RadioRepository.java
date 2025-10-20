package at.htlle.freq.domain;

import java.util.*;

public interface RadioRepository {
    Optional<Radio> findById(UUID id);
    List<Radio> findBySite(UUID siteId);
    Radio save(Radio radio);
    List<Radio> findAll();
}
