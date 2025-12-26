package at.htlle.freq.domain;

import java.util.UUID;

/**
 * Describes the telephony integration that is wired to a
 * {@link Site} location. The record keeps track of device model,
 * firmware and supported emergency capabilities to align with
 * regulatory requirements.
 */
public class PhoneIntegration {
    private UUID phoneIntegrationID;
    private UUID siteID;
    private String phoneType;    // Emergency / NonEmergency / Both
    private String phoneBrand;
    private String interfaceName;
    private Integer capacity;
    private String phoneFirmware;

    public PhoneIntegration() {}
    public PhoneIntegration(UUID phoneIntegrationID, UUID siteID, String phoneType,
                            String phoneBrand, String interfaceName, Integer capacity, String phoneFirmware) {
        this.phoneIntegrationID = phoneIntegrationID;
        this.siteID = siteID;
        this.phoneType = phoneType;
        this.phoneBrand = phoneBrand;
        this.interfaceName = interfaceName;
        this.capacity = capacity;
        this.phoneFirmware = phoneFirmware;
    }

    public UUID getPhoneIntegrationID() { return phoneIntegrationID; }
    public void setPhoneIntegrationID(UUID phoneIntegrationID) { this.phoneIntegrationID = phoneIntegrationID; }

    public UUID getSiteID() { return siteID; }
    public void setSiteID(UUID siteID) { this.siteID = siteID; }

    public String getPhoneType() { return phoneType; }
    public void setPhoneType(String phoneType) { this.phoneType = phoneType; }

    public String getPhoneBrand() { return phoneBrand; }
    public void setPhoneBrand(String phoneBrand) { this.phoneBrand = phoneBrand; }

    public String getInterfaceName() { return interfaceName; }
    public void setInterfaceName(String interfaceName) { this.interfaceName = interfaceName; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public String getPhoneFirmware() { return phoneFirmware; }
    public void setPhoneFirmware(String phoneFirmware) { this.phoneFirmware = phoneFirmware; }
}
