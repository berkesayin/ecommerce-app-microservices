package dev.berke.app.address.domain.model;

import lombok.Getter;
import org.springframework.util.Assert;

import java.util.UUID;

@Getter
public class Address {

    private String id;
    private String contactName;
    private String city;
    private String country;
    private String addressLine;
    private String zipCode;
    private boolean active;

    // no-args constructor for MongoDB
    protected Address() {
    }

    public Address(
            String contactName,
            String city,
            String country,
            String addressLine,
            String zipCode
    ) {
        validateAddress(contactName, city, country, addressLine);

        this.id = UUID.randomUUID().toString();
        this.contactName = contactName;
        this.city = city;
        this.country = country;
        this.addressLine = addressLine;
        this.zipCode = zipCode;
        this.active = false; // default is inactive
    }

    // domain, business

    public void updateDetails(
            String contactName,
            String city,
            String country,
            String addressLine,
            String zipCode
    ) {
        validateAddress(contactName, city, country, addressLine);

        this.contactName = contactName;
        this.city = city;
        this.country = country;
        this.addressLine = addressLine;
        this.zipCode = zipCode;
    }

    // encapsulation for state changes

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    private void validateAddress(
            String contactName,
            String city,
            String country,
            String addressLine
    ) {
        Assert.hasText(contactName, "Contact name cannot be empty");
        Assert.hasText(city, "City cannot be empty");
        Assert.hasText(country, "Country cannot be empty");
        Assert.hasText(addressLine, "Address line cannot be empty");
    }

    // equals/hashCode for ID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Address address = (Address) o;
        return id != null && id.equals(address.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}