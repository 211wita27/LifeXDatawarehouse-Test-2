package at.htlle.freq.domain;

import java.util.*;

public interface PhoneIntegrationRepository {
    Optional<PhoneIntegration> findById(UUID id);
    List<PhoneIntegration> findByClient(UUID clientId);
    void save(PhoneIntegration phone);
    List<PhoneIntegration> findAll();
}
