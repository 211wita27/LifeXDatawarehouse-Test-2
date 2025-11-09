package at.htlle.freq.domain;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
public class InstalledSoftwareFactory {
    /**
     * Links a {@link Software} package to a {@link Site} with an initial
     * {@link InstalledSoftwareStatus#OFFERED} state. The identifier stays unset
     * for database generation.
     *
     * @param siteID site that hosts the installation
     * @param softwareID software package being installed
     * @return transient installed software relationship
     */
    public InstalledSoftware create(UUID siteID, UUID softwareID) {
        return create(siteID, softwareID, LocalDate.now());
    }

    /**
     * Links a {@link Software} package to a {@link Site} with an initial
     * {@link InstalledSoftwareStatus#OFFERED} state and a custom offer date.
     *
     * @param siteID site that hosts the installation
     * @param softwareID software package being installed
     * @param offeredDate date when the offer became effective, {@code null} to skip auto-population
     * @return transient installed software relationship
     */
    public InstalledSoftware create(UUID siteID, UUID softwareID, LocalDate offeredDate) {
        String offered = offeredDate != null ? offeredDate.toString() : null;
        return new InstalledSoftware(null, siteID, softwareID,
                InstalledSoftwareStatus.OFFERED.dbValue(), offered, null);
    }
}
