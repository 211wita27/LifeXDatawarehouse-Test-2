package at.htlle.freq.domain;

import java.util.*;

public interface SoftwareRepository {
    Optional<Software> findById(UUID id);
    List<Software> findByName(String name);
    void save(Software software);
    List<Software> findAll();
}
