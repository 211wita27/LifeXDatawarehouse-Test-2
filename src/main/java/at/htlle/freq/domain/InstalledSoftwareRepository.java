package at.htlle.freq.domain;

import java.util.*;

public interface InstalledSoftwareRepository {
    Optional<InstalledSoftware> findById(UUID id);
    List<InstalledSoftware> findBySite(UUID siteId);
    List<InstalledSoftware> findBySoftware(UUID softwareId);
    InstalledSoftware save(InstalledSoftware isw);
    List<InstalledSoftware> findAll();
}
