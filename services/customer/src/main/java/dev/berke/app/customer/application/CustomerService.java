package dev.berke.app.customer.application;

import dev.berke.app.address.domain.model.Address;
import dev.berke.app.address.application.mapper.AddressMapper;
import dev.berke.app.address.api.dto.AddressRequest;
import dev.berke.app.address.api.dto.AddressResponse;
import dev.berke.app.customer.api.dto.CustomerCreateResponse;
import dev.berke.app.customer.api.dto.CustomerDataRequest;
import dev.berke.app.customer.api.dto.CustomerResponse;
import dev.berke.app.customer.api.dto.CustomerUpdateRequest;
import dev.berke.app.customer.application.mapper.CustomerMapper;
import dev.berke.app.customer.domain.model.Customer;
import dev.berke.app.customer.domain.repository.CustomerRepository;
import dev.berke.app.shared.exception.AddressNotFoundException;
import dev.berke.app.shared.exception.CustomerAlreadyExistsException;
import dev.berke.app.shared.exception.CustomerNotFoundException;
import dev.berke.app.shared.exception.InvalidRequestException;
import dev.berke.app.shared.exception.NoActiveAddressFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final AddressMapper addressMapper;

    @Transactional
    public CustomerCreateResponse createCustomer(CustomerDataRequest request) {
        if (customerRepository.existsByEmail(request.email())) {
            throw new CustomerAlreadyExistsException(
                    String.format("Customer with email '%s' already exists.", request.email())
            );
        }

        Customer customer = customerMapper.toCustomer(request);
        Customer savedCustomer = customerRepository.save(customer);

        log.info("Customer created with ID: {}", savedCustomer.getId());

        return new CustomerCreateResponse(savedCustomer.getId());
    }

    @Transactional
    public CustomerResponse updateProfile(CustomerUpdateRequest request, String customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(
                        String.format("Customer not found with ID: %s", customerId)
                ));

        updateCustomerFields(customer, request);

        Customer savedCustomer = customerRepository.save(customer);
        return customerMapper.fromCustomer(savedCustomer);
    }

    public CustomerResponse getProfile(String customerId) {
        return customerRepository.findById(customerId)
                .map(customerMapper::fromCustomer)
                .orElseThrow(() -> new CustomerNotFoundException(
                        String.format("Customer not found with ID: %s", customerId)
                ));
    }

    public Boolean checkCustomerById(String customerId) {
        return customerRepository.findById(customerId).isPresent();
    }

    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll()
                .stream()
                .map(customerMapper::fromCustomer)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteProfile(String customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException(
                    String.format("Customer not found with ID: %s", customerId)
            );
        }

        customerRepository.deleteById(customerId);
    }

    @Transactional
    public AddressResponse addBillingAddress(String customerId, AddressRequest addressRequest) {
        Customer customer = getCustomerOrThrow(customerId);
        Address newAddress = addressMapper.toAddress(addressRequest);

        if (customer.getBillingAddresses() == null) {
            customer.setBillingAddresses(new ArrayList<>());
        }

        customer.getBillingAddresses().add(newAddress);

        if (Boolean.TRUE.equals(addressRequest.isActive()) || customer.getBillingAddresses().size() == 1) {
            newAddress.setIsActive(true);
            customer.getBillingAddresses().stream()
                    .filter(a -> !a.equals(newAddress))
                    .forEach(a -> a.setIsActive(false));

            customer.setActiveBillingAddressId(newAddress.getId());
        } else {
            newAddress.setIsActive(false);
        }

        customerRepository.save(customer);
        return addressMapper.toAddressResponse(newAddress);
    }

    @Transactional
    public AddressResponse addShippingAddress(String customerId, AddressRequest addressRequest) {
        Customer customer = getCustomerOrThrow(customerId);
        Address newAddress = addressMapper.toAddress(addressRequest);

        if (customer.getShippingAddresses() == null) {
            customer.setShippingAddresses(new ArrayList<>());
        }

        customer.getShippingAddresses().add(newAddress);

        if (Boolean.TRUE.equals(addressRequest.isActive()) || customer.getShippingAddresses().size() == 1) {
            newAddress.setIsActive(true);
            customer.getShippingAddresses().stream()
                    .filter(a -> !a.equals(newAddress))
                    .forEach(a -> a.setIsActive(false));

            customer.setActiveShippingAddressId(newAddress.getId());
        } else {
            newAddress.setIsActive(false);
        }

        customerRepository.save(customer);
        return addressMapper.toAddressResponse(newAddress);
    }

    public List<AddressResponse> getBillingAddresses(String customerId) {
        Customer customer = getCustomerOrThrow(customerId);

        List<Address> addresses = customer.getBillingAddresses() != null
                ? customer.getBillingAddresses()
                : List.of();

        return addresses.stream()
                .map(addressMapper::toAddressResponse)
                .collect(Collectors.toList());
    }

    public List<AddressResponse> getShippingAddresses(String customerId) {
        Customer customer = getCustomerOrThrow(customerId);

        List<Address> addresses = customer.getShippingAddresses() != null
                ? customer.getShippingAddresses()
                : List.of();

        return addresses.stream()
                .map(addressMapper::toAddressResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void setActiveBillingAddress(String customerId, String billingAddressId) {
        Customer customer = getCustomerOrThrow(customerId);

        Address addressToActivate = findAddressById(
                customer.getBillingAddresses(),
                billingAddressId,
                "Billing"
        );

        if (Boolean.TRUE.equals(addressToActivate.getIsActive())) {
            throw new InvalidRequestException("This billing address is already active.");
        }

        if (customer.getBillingAddresses() != null) {
            customer.getBillingAddresses().forEach(address -> address.setIsActive(false));
        }

        addressToActivate.setIsActive(true);
        customer.setActiveBillingAddressId(billingAddressId);

        customerRepository.save(customer);
    }

    @Transactional
    public void setActiveShippingAddress(String customerId, String shippingAddressId) {
        Customer customer = getCustomerOrThrow(customerId);

        Address addressToActivate = findAddressById(
                customer.getShippingAddresses(),
                shippingAddressId,
                "Shipping"
        );

        if (Boolean.TRUE.equals(addressToActivate.getIsActive())) {
            throw new InvalidRequestException("This shipping address is already active.");
        }

        if (customer.getShippingAddresses() != null) {
            customer.getShippingAddresses().forEach(address -> address.setIsActive(false));
        }

        addressToActivate.setIsActive(true);
        customer.setActiveShippingAddressId(shippingAddressId);

        customerRepository.save(customer);
    }

    public AddressResponse getActiveBillingAddress(String customerId) {
        Customer customer = getCustomerOrThrow(customerId);

        String activeId = customer.getActiveBillingAddressId();

        if (!StringUtils.hasText(activeId)) {
            throw new NoActiveAddressFoundException(
                    String.format("No active billing address set for customer: %s", customerId)
            );
        }

        Address activeBillingAddress = findAddressById(
                customer.getBillingAddresses(),
                activeId,
                "Billing"
        );

        return addressMapper.toAddressResponse(activeBillingAddress);
    }

    public AddressResponse getActiveShippingAddress(String customerId) {
        Customer customer = getCustomerOrThrow(customerId);

        String activeId = customer.getActiveShippingAddressId();

        if (!StringUtils.hasText(activeId)) {
            throw new NoActiveAddressFoundException(
                    String.format("No active shipping address set for customer: %s", customerId)
            );
        }

        Address activeShippingAddress = findAddressById(
                customer.getShippingAddresses(),
                activeId,
                "Shipping"
        );

        return addressMapper.toAddressResponse(activeShippingAddress);
    }

    // helper methods
    private Customer getCustomerOrThrow(String customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(
                        String.format("Customer not found with ID: %s", customerId)
                ));
    }

    private Address findAddressById(List<Address> addresses, String addressId, String type) {
        if (addresses == null) {
            throw new AddressNotFoundException(
                    String.format("%s address not found with ID: %s", type, addressId)
            );
        }

        return addresses.stream()
                .filter(a -> a.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new AddressNotFoundException(
                        String.format("%s address not found with ID: %s", type, addressId)
                ));
    }

    private void updateCustomerFields(Customer customer, CustomerUpdateRequest request) {
        if (StringUtils.hasText(request.name())) {
            customer.setName(request.name());
        }
        if (StringUtils.hasText(request.surname())) {
            customer.setSurname(request.surname());
        }
        if (StringUtils.hasText(request.gsmNumber())) {
            customer.setGsmNumber(request.gsmNumber());
        }
        if (StringUtils.hasText(request.email())) {
            customer.setEmail(request.email());
        }
        if (StringUtils.hasText(request.password())) {
            customer.setPassword(request.password());
        }
        if (StringUtils.hasText(request.identityNumber())) {
            customer.setIdentityNumber(request.identityNumber());
        }
        if (request.registrationAddress() != null) {
            customer.setRegistrationAddress(request.registrationAddress());
        }
    }
}