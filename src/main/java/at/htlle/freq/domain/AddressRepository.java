package at.htlle.freq.domain;

import java.util.*;

public interface AddressRepository {
    Optional<Address> findById(UUID id);
    void save(Address address);
    List<Address> findAll();
}
