package at.htlle.freq.domain;

import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class RadioFactory {
    public Radio create(UUID siteID, UUID assignedClientID, String brand, String serialNr,
                        String mode, String digitalStandard) {
        return new Radio(null, siteID, assignedClientID, brand, serialNr, mode, digitalStandard);
    }
}
