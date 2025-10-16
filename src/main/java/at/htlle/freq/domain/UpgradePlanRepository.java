package at.htlle.freq.domain;

import java.util.*;

public interface UpgradePlanRepository {
    Optional<UpgradePlan> findById(UUID id);
    List<UpgradePlan> findBySite(UUID siteId);
    void save(UpgradePlan plan);
    List<UpgradePlan> findAll();
}
