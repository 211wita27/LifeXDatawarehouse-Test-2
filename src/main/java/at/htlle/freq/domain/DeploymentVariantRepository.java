package at.htlle.freq.domain;

import java.util.*;

public interface DeploymentVariantRepository {
    Optional<DeploymentVariant> findById(UUID id);
    Optional<DeploymentVariant> findByCode(String code);
    Optional<DeploymentVariant> findByName(String name);
    DeploymentVariant save(DeploymentVariant dv);
    List<DeploymentVariant> findAll();
}
