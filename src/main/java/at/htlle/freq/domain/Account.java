package at.htlle.freq.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    private int AccountID;
    private String AccountName;
    private String ContactEmail;
    private String ContactPhone;
    private String VATNumber;
    private String Country;

    public int getAccountID() {
        return AccountID;
    }

    public String getAccountName() {
        return AccountName;
    }

    public String getContactEmail() {
        return ContactEmail;
    }

    public String getContactPhone() {
        return ContactPhone;
    }

    public String getVATNumber() {
        return VATNumber;
    }

    public String getCountry() {
        return Country;
    }


    public void setAccountName(String accountName) {
        AccountName = accountName;
    }

    public void setContactEmail(String contactEmail) {
        ContactEmail = contactEmail;
    }

    public void setContactPhone(String contactPhone) {
        ContactPhone = contactPhone;
    }

    public void setVATNumber(String VATNumber) {
        this.VATNumber = VATNumber;
    }

    public void setCountry(String country) {
        Country = country;
    }

}
//CREATE TABLE Account (
//                         AccountID     INT          AUTO_INCREMENT PRIMARY KEY,
//                         AccountName   VARCHAR(150) NOT NULL,
//                         ContactName   VARCHAR(100) NOT NULL,
//                         ContactEmail  VARCHAR(100) NOT NULL,
//                         ContactPhone  VARCHAR(30)  NOT NULL,
//                         VATNumber     VARCHAR(30)  NOT NULL,
//                         Country       VARCHAR(50)  NOT NULL
//);