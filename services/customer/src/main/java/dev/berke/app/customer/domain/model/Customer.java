package dev.berke.app.customer.domain.model;

import dev.berke.app.address.domain.model.Address;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Document
public class Customer {

    @Id
    private String id;
    private String name;
    private String surname;
    private String gsmNumber;
    private String email;
    private String password;
    private String identityNumber;
    private String registrationAddress;
    private List<Address> billingAddresses;
    private List<Address> shippingAddresses;
    private String activeBillingAddressId;
    private String activeShippingAddressId;

    @LastModifiedDate
    private Instant updatedAt;

    public Customer(String name, String surname, String gsmNumber, String email) {
        this.name = name;
        this.surname = surname;
        this.gsmNumber = gsmNumber;
        this.email = email;
    }
}