package dev.berke.app.customer.application;

import dev.berke.app.address.domain.model.Address;
import dev.berke.app.address.application.mapper.AddressMapper;
import dev.berke.app.address.api.dto.AddressCreateRequest;
import dev.berke.app.address.api.dto.AddressResponse;
import dev.berke.app.customer.api.dto.CustomerCreateResponse;
import dev.berke.app.customer.api.dto.CustomerDataRequest;
import dev.berke.app.customer.api.dto.CustomerDetailResponse;
import dev.berke.app.customer.api.dto.CustomerSummaryResponse;
import dev.berke.app.customer.api.dto.CustomerUpdateResponse;
import dev.berke.app.customer.api.dto.UpdateContactRequest;
import dev.berke.app.customer.api.dto.UpdateIdentityRequest;
import dev.berke.app.customer.application.mapper.CustomerMapper;
import dev.berke.app.customer.domain.model.Customer;
import dev.berke.app.customer.domain.repository.CustomerRepository;
import dev.berke.app.shared.exception.AddressNotFoundException;
import dev.berke.app.shared.exception.CustomerAlreadyExistsException;
import dev.berke.app.shared.exception.CustomerNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
            throw new CustomerAlreadyExistsException(request.email());
        }

        Customer customer = customerMapper.toCustomer(request);
        Customer savedCustomer = customerRepository.save(customer);

        log.info("Customer created with ID: {}", savedCustomer.getId());
        return new CustomerCreateResponse(savedCustomer.getId());
    }

    @Transactional
    public CustomerUpdateResponse updateIdentity(
            String customerId,
            UpdateIdentityRequest updateIdentityRequest
    ) {
        Customer customer = getCustomerOrThrow(customerId);

        customer.updateIdentity(
                updateIdentityRequest.name(),
                updateIdentityRequest.surname(),
                updateIdentityRequest.identityNumber()
        );

        return customerMapper.toCustomerUpdateResponse(customerRepository.save(customer));
    }

    @Transactional
    public CustomerUpdateResponse updateContact(
            String customerId,
            UpdateContactRequest updateContactRequest
    ) {
        Customer customer = getCustomerOrThrow(customerId);

        customer.updateContactInfo(
                updateContactRequest.email(),
                updateContactRequest.gsmNumber(),
                updateContactRequest.registrationAddress()
        );

        return customerMapper.toCustomerUpdateResponse(customerRepository.save(customer));
    }

    @Transactional(readOnly = true)
    public CustomerDetailResponse getProfile(String customerId) {
        return customerRepository.findById(customerId)
                .map(customerMapper::toCustomerDetailResponse)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
    }

    @Transactional
    public void deleteProfile(String customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException(customerId);
        }

        customerRepository.deleteById(customerId);
    }

    @Transactional
    public AddressResponse addBillingAddress(
            String customerId,
            AddressCreateRequest addressCreateRequest
    ) {
        Customer customer = getCustomerOrThrow(customerId);
        Address address = addressMapper.toAddress(addressCreateRequest);

        customer.addBillingAddress(address);
        customerRepository.save(customer);

        return addressMapper.toAddressResponse(address);
    }

    @Transactional
    public AddressResponse addShippingAddress(
            String customerId, AddressCreateRequest addressCreateRequest
    ) {
        Customer customer = getCustomerOrThrow(customerId);
        Address address = addressMapper.toAddress(addressCreateRequest);

        customer.addShippingAddress(address);
        customerRepository.save(customer);

        return addressMapper.toAddressResponse(address);
    }

    @Transactional
    public void setActiveBillingAddress(String customerId, String addressId) {
        Customer customer = getCustomerOrThrow(customerId);
        customer.activateBillingAddress(addressId);

        customerRepository.save(customer);
    }

    @Transactional
    public void setActiveShippingAddress(String customerId, String addressId) {
        Customer customer = getCustomerOrThrow(customerId);
        customer.activateShippingAddress(addressId);

        customerRepository.save(customer);
    }

    @Transactional
    public void deleteBillingAddress(String customerId, String addressId) {
        Customer customer = getCustomerOrThrow(customerId);
        customer.removeBillingAddress(addressId);

        customerRepository.save(customer);
    }

    @Transactional
    public void deleteShippingAddress(String customerId, String addressId) {
        Customer customer = getCustomerOrThrow(customerId);
        customer.removeShippingAddress(addressId);

        customerRepository.save(customer);
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> getBillingAddresses(String customerId) {
        return getCustomerOrThrow(customerId)
                .getBillingAddresses()
                .stream()
                .map(addressMapper::toAddressResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> getShippingAddresses(String customerId) {
        return getCustomerOrThrow(customerId)
                .getShippingAddresses()
                .stream()
                .map(addressMapper::toAddressResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AddressResponse getActiveBillingAddress(String customerId) {
        Customer customer = getCustomerOrThrow(customerId);

        return customer.getActiveBillingAddress()
                .map(addressMapper::toAddressResponse)
                .orElseThrow(() ->
                        new AddressNotFoundException("No active billing address found")
                );
    }

    @Transactional(readOnly = true)
    public AddressResponse getActiveShippingAddress(String customerId) {
        Customer customer = getCustomerOrThrow(customerId);

        return customer.getActiveShippingAddress()
                .map(addressMapper::toAddressResponse)
                .orElseThrow(() ->
                        new AddressNotFoundException("No active shipping address found")
                );
    }

    @Transactional(readOnly = true)
    public List<CustomerSummaryResponse> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(customerMapper::toCustomerSummaryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean checkCustomerById(String customerId) {
        return customerRepository.existsById(customerId);
    }

    private Customer getCustomerOrThrow(String customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
    }
}