package at.htlle.freq.domain;

import java.util.UUID;

public class Account {
    private UUID accountID;
    private String accountName;
    private String contactName;
    private String contactEmail;
    private String contactPhone;
    private String vatNumber;
    private String country;

    public Account() {}
    public Account(UUID accountID, String accountName, String contactName, String contactEmail,
                   String contactPhone, String vatNumber, String country) {
        this.accountID = accountID;
        this.accountName = accountName;
        this.contactName = contactName;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.vatNumber = vatNumber;
        this.country = country;
    }

    public UUID getAccountID() { return accountID; }
    public void setAccountID(UUID accountID) { this.accountID = accountID; }

    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }

    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getVatNumber() { return vatNumber; }
    public void setVatNumber(String vatNumber) { this.vatNumber = vatNumber; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
}
