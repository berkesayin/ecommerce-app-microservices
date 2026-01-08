package dev.berke.app.address.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Address {

    private String id;
    private String contactName;
    private String city;
    private String country;
    private String address;
    private String zipCode;
    private Boolean isActive;
}