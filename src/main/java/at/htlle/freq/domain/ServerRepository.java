package at.htlle.freq.domain;

import java.util.*;

public interface ServerRepository {
    Optional<Server> findById(UUID id);
    List<Server> findBySite(UUID siteId);
    Server save(Server server);
    List<Server> findAll();
}
