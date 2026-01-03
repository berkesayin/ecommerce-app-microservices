package dev.berke.app.customer.api.dto;

import dev.berke.app.address.api.dto.AddressResponse;

import java.util.List;

public record CustomerDetailResponse(
        String id,
        String name,
        String surname,
        String gsmNumber,
        String email,
        String registrationAddress,
        List<AddressResponse> billingAddresses,
        List<AddressResponse> shippingAddresses,
        String activeBillingAddressId,
        String activeShippingAddressId
) {
}
