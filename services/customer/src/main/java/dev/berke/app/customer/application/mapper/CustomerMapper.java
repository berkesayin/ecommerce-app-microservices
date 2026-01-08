package dev.berke.app.customer.application.mapper;

import dev.berke.app.address.api.dto.AddressResponse;
import dev.berke.app.address.application.mapper.AddressMapper;
import dev.berke.app.address.domain.model.Address;
import dev.berke.app.customer.api.dto.CustomerDataRequest;
import dev.berke.app.customer.api.dto.CustomerDetailResponse;
import dev.berke.app.customer.api.dto.CustomerSummaryResponse;
import dev.berke.app.customer.api.dto.CustomerUpdateResponse;
import dev.berke.app.customer.domain.model.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CustomerMapper {

    private final AddressMapper addressMapper;

    // createCustomer
    public Customer toCustomer(CustomerDataRequest request) {
        if (request == null) {
            return null;
        }

        return Customer.builder()
                .name(request.name())
                .surname(request.surname())
                .gsmNumber(request.gsmNumber())
                .email(request.email())
                .build();
    }

    // getProfile and getCustomerById
    public CustomerDetailResponse toDetailResponse(Customer customer) {
        if (customer == null) {
            return null;
        }

        List<AddressResponse> billingResponses = toAddressResponseList(customer.getBillingAddresses());
        List<AddressResponse> shippingResponses = toAddressResponseList(customer.getShippingAddresses());

        return new CustomerDetailResponse(
                customer.getId(),
                customer.getName(),
                customer.getSurname(),
                customer.getGsmNumber(),
                customer.getEmail(),
                customer.getRegistrationAddress(),
                customer.getIdentityNumber(),
                billingResponses,
                shippingResponses,
                customer.getActiveBillingAddressId(),
                customer.getActiveShippingAddressId()
        );
    }

    // getAllCustomers
    public CustomerSummaryResponse toSummaryResponse(Customer customer) {
        if (customer == null) {
            return null;
        }

        return new CustomerSummaryResponse(
                customer.getId(),
                customer.getName(),
                customer.getSurname(),
                customer.getGsmNumber(),
                customer.getEmail()
        );
    }

    // updateProfile
    public CustomerUpdateResponse toUpdateResponse(Customer customer) {
        if (customer == null) {
            return null;
        }

        return new CustomerUpdateResponse(
                customer.getId(),
                customer.getName(),
                customer.getSurname(),
                customer.getEmail(),
                customer.getUpdatedAt()
        );
    }

    private List<AddressResponse> toAddressResponseList(List<Address> addresses) {
        if (addresses == null) {
            return Collections.emptyList();
        }
        return addresses.stream()
                .map(addressMapper::toAddressResponse)
                .collect(Collectors.toList());
    }
}