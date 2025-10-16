package at.htlle.freq.domain;

import java.util.*;

public interface ServiceContractRepository {
    Optional<ServiceContract> findById(UUID id);
    List<ServiceContract> findByAccount(UUID accountId);
    void save(ServiceContract contract);
    List<ServiceContract> findAll();
}
