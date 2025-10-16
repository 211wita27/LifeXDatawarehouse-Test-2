package at.htlle.freq.domain;

import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class PhoneIntegrationFactory {
    public PhoneIntegration create(UUID clientID, String phoneType, String brand, String serialNr, String firmware) {
        return new PhoneIntegration(null, clientID, phoneType, brand, serialNr, firmware);
    }
}
