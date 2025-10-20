package at.htlle.freq.domain;

import java.util.*;

public interface AudioDeviceRepository {
    Optional<AudioDevice> findById(UUID id);
    List<AudioDevice> findByClient(UUID clientId);
    AudioDevice save(AudioDevice device);
    List<AudioDevice> findAll();
}
