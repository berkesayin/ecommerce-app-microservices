package dev.berke.app.address.application.mapper;

import dev.berke.app.address.api.dto.AddressRequest;
import dev.berke.app.address.api.dto.AddressResponse;
import dev.berke.app.address.domain.model.Address;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class AddressMapper {

    public Address toAddress(AddressRequest addressRequest) {
        if(addressRequest == null) {
            return null;
        }
        return Address.builder()
                .id(UUID.randomUUID().toString())
                .contactName(addressRequest.contactName())
                .city(addressRequest.city())
                .country(addressRequest.country())
                .address(addressRequest.address())
                .zipCode(addressRequest.zipCode())
                .isActive(addressRequest.isActive())
                .build();
    }

    public AddressResponse toAddressResponse(Address address) {
        if (address == null) {
            return null;
        }
        return new AddressResponse(
                address.getId(),
                address.getContactName(),
                address.getCity(),
                address.getCountry(),
                address.getAddress(),
                address.getZipCode(),
                address.getIsActive()
        );
    }
}