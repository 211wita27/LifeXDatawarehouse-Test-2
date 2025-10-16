package at.htlle.freq.domain;

import java.util.UUID;

public class Address {
    private UUID addressID;
    private String street;
    private String cityID; // FK -> City.cityID

    public Address() {}
    public Address(UUID addressID, String street, String cityID) {
        this.addressID = addressID;
        this.street = street;
        this.cityID = cityID;
    }

    public UUID getAddressID() { return addressID; }
    public void setAddressID(UUID addressID) { this.addressID = addressID; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getCityID() { return cityID; }
    public void setCityID(String cityID) { this.cityID = cityID; }
}
