package at.htlle.freq.domain;

import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class AudioDeviceFactory {
    public AudioDevice create(UUID clientID, String brand, String serialNr, String firmware, String deviceType) {
        return new AudioDevice(null, clientID, brand, serialNr, firmware, deviceType);
    }
}
