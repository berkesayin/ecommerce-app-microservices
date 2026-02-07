package dev.berke.app.address.application.mapper;

import dev.berke.app.address.api.dto.AddressCreateRequest;
import dev.berke.app.address.api.dto.AddressResponse;
import dev.berke.app.address.domain.model.Address;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper {

    public Address toAddress(AddressCreateRequest addressCreateRequest) {
        if (addressCreateRequest == null) {
            return null;
        }

        return new Address(
                addressCreateRequest.contactName(),
                addressCreateRequest.city(),
                addressCreateRequest.country(),
                addressCreateRequest.addressLine(),
                addressCreateRequest.zipCode()
        );
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
                address.getAddressLine(),
                address.getZipCode(),
                address.isActive()
        );
    }
}