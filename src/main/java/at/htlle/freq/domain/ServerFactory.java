package at.htlle.freq.domain;

import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class ServerFactory {
    public Server create(UUID siteID, String serverName, String serverBrand, String serverSerialNr,
                         String serverOS, String patchLevel, String virtualPlatform,
                         String virtualVersion, boolean highAvailability) {
        return new Server(null, siteID, serverName, serverBrand, serverSerialNr,
                serverOS, patchLevel, virtualPlatform, virtualVersion, highAvailability);
    }
}
