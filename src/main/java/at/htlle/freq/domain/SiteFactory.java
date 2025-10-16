package at.htlle.freq.domain;

import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class SiteFactory {
    public Site create(String siteName, UUID projectID, UUID addressID, String fireZone, Integer tenantCount) {
        return new Site(null, siteName, projectID, addressID, fireZone, tenantCount);
    }
}
