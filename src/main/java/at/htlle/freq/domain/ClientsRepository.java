package at.htlle.freq.domain;

import java.util.*;

public interface ClientsRepository {
    Optional<Clients> findById(UUID id);
    List<Clients> findBySite(UUID siteId);
    Clients save(Clients client);
    List<Clients> findAll();
}
