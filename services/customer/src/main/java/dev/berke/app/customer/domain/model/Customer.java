package dev.berke.app.customer.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.berke.app.address.domain.model.Address;
import dev.berke.app.shared.exception.AddressNotFoundException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Getter
@NoArgsConstructor
@Document(collection = "customers")
public class Customer {

    @Id
    private String id;
    private String name;
    private String surname;
    private String email;
    private String gsmNumber;

    @JsonIgnore
    private String password;

    @JsonIgnore
    private String identityNumber;

    private String registrationAddress;
    private List<Address> billingAddresses = new ArrayList<>();
    private List<Address> shippingAddresses = new ArrayList<>();

    @LastModifiedDate
    private Instant updatedAt;

    public Customer(String name, String surname, String email, String gsmNumber) {
        Assert.hasText(email, "Email is required");
        Assert.hasText(name, "Name is required");
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.gsmNumber = gsmNumber;
    }

    // DDD (domain driven design and business)

    public void updateIdentity(
            String name,
            String surname,
            String identityNumber
    ) {
        if (StringUtils.hasText(name)) this.name = name;
        if (StringUtils.hasText(surname)) this.surname = surname;
        if (StringUtils.hasText(identityNumber)) this.identityNumber = identityNumber;
    }

    public void updateContactInfo(
            String email,
            String gsmNumber,
            String registrationAddress
    ) {
        if (StringUtils.hasText(email)) this.email = email;
        if (StringUtils.hasText(gsmNumber)) this.gsmNumber = gsmNumber;
        if (StringUtils.hasText(registrationAddress))
            this.registrationAddress = registrationAddress;
    }

    public void changePassword(String newPasswordHash) {
        Assert.hasText(newPasswordHash, "Password hash cannot be empty");
        this.password = newPasswordHash;
    }

    public void addBillingAddress(Address address) {
        addAddress(this.billingAddresses, address);
    }

    public void addShippingAddress(Address address) {
        addAddress(this.shippingAddresses, address);
    }

    public void activateBillingAddress(String addressId) {
        activateAddress(this.billingAddresses, addressId);
    }

    public void activateShippingAddress(String addressId) {
        activateAddress(this.shippingAddresses, addressId);
    }

    public void removeBillingAddress(String addressId) {
        removeAddress(this.billingAddresses, addressId, "Billing");
    }

    public void removeShippingAddress(String addressId) {
        removeAddress(this.shippingAddresses, addressId, "Shipping");
    }

    public List<Address> getBillingAddresses() {
        return Collections.unmodifiableList(billingAddresses);
    }

    public List<Address> getShippingAddresses() {
        return Collections.unmodifiableList(shippingAddresses);
    }

    public Optional<Address> getActiveBillingAddress() {
        return this.billingAddresses
                .stream()
                .filter(Address::isActive)
                .findFirst();
    }

    public Optional<Address> getActiveShippingAddress() {
        return this.shippingAddresses
                .stream()
                .filter(Address::isActive)
                .findFirst();
    }

    // encapsulations

    // if first address -> active
    // if an address is activated -> others deactivated
    private void addAddress(List<Address> addressList, Address newAddress) {
        if (addressList.isEmpty()) {
            newAddress.activate();
        } else {
            if (newAddress.isActive()) {
                addressList.forEach(Address::deactivate);
            }
        }
        addressList.add(newAddress);
    }

    private void activateAddress(List<Address> addressList, String addressId) {
        Address addressToActivate = addressList.stream()
                .filter(a -> a.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() ->
                        new AddressNotFoundException("Address not found with ID: " + addressId)
                );

        if (addressToActivate.isActive()) {
            return;
        }

        addressList.forEach(Address::deactivate);
        addressToActivate.activate();
    }

    private void removeAddress(
            List<Address> addresses,
            String addressId,
            String type
    ) {
        boolean removed = addresses.removeIf(address -> address.getId().equals(addressId));

        if (!removed) {
            throw new AddressNotFoundException(
                    String.format("%s address with ID %s not found.", type, addressId)
            );
        }
    }
}