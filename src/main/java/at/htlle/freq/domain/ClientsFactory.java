package at.htlle.freq.domain;

import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class ClientsFactory {
    public Clients create(UUID siteID, String clientName, String clientBrand, String clientSerialNr,
                          String clientOS, String patchLevel, String installType) {
        return new Clients(null, siteID, clientName, clientBrand, clientSerialNr, clientOS, patchLevel, installType);
    }
}
