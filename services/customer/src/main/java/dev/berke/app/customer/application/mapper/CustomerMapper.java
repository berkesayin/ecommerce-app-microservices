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

@Component
@RequiredArgsConstructor
public class CustomerMapper {

    private final AddressMapper addressMapper;

    // register, create customer
    public Customer toCustomer(CustomerDataRequest request) {
        if (request == null) return null;

        return new Customer(
                request.name(),
                request.surname(),
                request.email(),
                request.gsmNumber()
        );
    }

    // GET /me
    public CustomerDetailResponse toCustomerDetailResponse(Customer customer) {
        if (customer == null) return null;

        return new CustomerDetailResponse(
                customer.getId(),
                customer.getName(),
                customer.getSurname(),
                customer.getGsmNumber(),
                customer.getEmail(),
                customer.getRegistrationAddress(),
                customer.getIdentityNumber(),
                toAddressResponseList(customer.getBillingAddresses()),
                toAddressResponseList(customer.getShippingAddresses()),
                customer.getActiveBillingAddress().map(Address::getId).orElse(null),
                customer.getActiveShippingAddress().map(Address::getId).orElse(null)
        );
    }

    // for backoffice
    public CustomerSummaryResponse toCustomerSummaryResponse(Customer customer) {
        if (customer == null) return null;

        return new CustomerSummaryResponse(
                customer.getId(),
                customer.getName(),
                customer.getSurname(),
                customer.getGsmNumber(),
                customer.getEmail()
        );
    }

    // PATCH /me/identity and PATCH /me/contact
    public CustomerUpdateResponse toCustomerUpdateResponse(Customer customer) {
        if (customer == null) return null;

        return new CustomerUpdateResponse(
                customer.getId(),
                customer.getName(),
                customer.getSurname(),
                customer.getEmail(),
                customer.getGsmNumber(),
                customer.getUpdatedAt()
        );
    }

    private List<AddressResponse> toAddressResponseList(List<Address> addresses) {
        if (addresses == null || addresses.isEmpty()) {
            return Collections.emptyList();
        }

        return addresses.stream()
                .map(addressMapper::toAddressResponse)
                .toList();
    }
}