package at.htlle.freq.domain;

import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class SiteFactory {
    /**
     * Creates a {@link Site} bound to a project and address. The site identifier
     * remains {@code null} until persisted.
     *
     * @param siteName descriptive name of the location
     * @param projectID parent {@link Project} identifier
     * @param addressID reference to the {@link Address}
     * @param fireZone fire zone classification
     * @param tenantCount number of tenants residing at the site
     * @param highAvailability whether the site is configured for high availability
     * @return transient site entity
     */
    public Site create(String siteName, UUID projectID, UUID addressID, String fireZone, Integer tenantCount,
                       boolean highAvailability) {
        return new Site(null, siteName, projectID, addressID, fireZone, tenantCount, highAvailability);
    }
}
