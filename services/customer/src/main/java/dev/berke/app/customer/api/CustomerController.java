package dev.berke.app.customer.api;

import dev.berke.app.address.api.dto.AddressCreateRequest;
import dev.berke.app.address.api.dto.AddressResponse;
import dev.berke.app.customer.api.dto.CustomerCreateResponse;
import dev.berke.app.customer.api.dto.CustomerDataRequest;
import dev.berke.app.customer.api.dto.CustomerDetailResponse;
import dev.berke.app.customer.api.dto.CustomerSummaryResponse;
import dev.berke.app.customer.api.dto.CustomerUpdateResponse;
import dev.berke.app.customer.api.dto.UpdateContactRequest;
import dev.berke.app.customer.api.dto.UpdateIdentityRequest;
import dev.berke.app.customer.application.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<CustomerCreateResponse> createCustomer(
            @RequestBody @Valid CustomerDataRequest customerDataRequest
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(customerService.createCustomer(customerDataRequest));
    }

    @PatchMapping("/me/identity")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CustomerUpdateResponse> updateIdentity(
            @AuthenticationPrincipal String customerIdPrincipal,
            @RequestBody @Valid UpdateIdentityRequest request
    ) {
        return ResponseEntity.ok(customerService.updateIdentity(customerIdPrincipal, request));
    }

    @PatchMapping("/me/contact")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CustomerUpdateResponse> updateContact(
            @AuthenticationPrincipal String customerIdPrincipal,
            @RequestBody @Valid UpdateContactRequest request
    ) {
        return ResponseEntity.ok(customerService.updateContact(customerIdPrincipal, request));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CustomerDetailResponse> getProfile(
            @AuthenticationPrincipal String customerIdPrincipal
    ) {
        return ResponseEntity.ok(customerService.getProfile(customerIdPrincipal));
    }

    @DeleteMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteProfile(
            @AuthenticationPrincipal String customerIdPrincipal
    ) {
        customerService.deleteProfile(customerIdPrincipal);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/me/billing-addresses")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<AddressResponse> addBillingAddress(
            @AuthenticationPrincipal String customerIdPrincipal,
            @RequestBody @Valid AddressCreateRequest addressCreateRequest
    ) {
        return ResponseEntity.ok(customerService
                .addBillingAddress(customerIdPrincipal, addressCreateRequest));
    }

    @PostMapping("/me/shipping-addresses")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<AddressResponse> addShippingAddress(
            @AuthenticationPrincipal String customerIdPrincipal,
            @RequestBody @Valid AddressCreateRequest addressCreateRequest
    ) {
        return ResponseEntity.ok(customerService
                .addShippingAddress(customerIdPrincipal, addressCreateRequest));
    }

    @PutMapping("/me/billing-addresses/{billingAddressId}/active")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> setActiveBillingAddress(
            @AuthenticationPrincipal String customerIdPrincipal,
            @PathVariable("billingAddressId") String billingAddressId
    ) {
        customerService.setActiveBillingAddress(customerIdPrincipal, billingAddressId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/me/shipping-addresses/{shippingAddressId}/active")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> setActiveShippingAddress(
            @AuthenticationPrincipal String customerIdPrincipal,
            @PathVariable("shippingAddressId") String shippingAddressId
    ) {
        customerService.setActiveShippingAddress(customerIdPrincipal, shippingAddressId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me/billing-addresses/{billingAddressId}")
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBillingAddress(
            @AuthenticationPrincipal String customerIdPrincipal,
            @PathVariable("billingAddressId") String billingAddressId
    ) {
        customerService.deleteBillingAddress(customerIdPrincipal, billingAddressId);
    }

    @DeleteMapping("/me/shipping-addresses/{shippingAddressId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteShippingAddress(
            @AuthenticationPrincipal String customerIdPrincipal,
            @PathVariable("shippingAddressId") String shippingAddressId
    ) {
        customerService.deleteShippingAddress(customerIdPrincipal, shippingAddressId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me/billing-addresses")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<AddressResponse>> getBillingAddresses(
            @AuthenticationPrincipal String customerIdPrincipal
    ) {
        return ResponseEntity.ok(customerService.getBillingAddresses(customerIdPrincipal));
    }

    @GetMapping("/me/shipping-addresses")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<AddressResponse>> getShippingAddresses(
            @AuthenticationPrincipal String customerIdPrincipal
    ) {
        return ResponseEntity.ok(customerService.getShippingAddresses(customerIdPrincipal));
    }

    @GetMapping("/me/billing-addresses/active")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<AddressResponse> getActiveBillingAddress(
            @AuthenticationPrincipal String customerIdPrincipal
    ) {
        return ResponseEntity.ok(customerService.getActiveBillingAddress(customerIdPrincipal));
    }

    @GetMapping("/me/shipping-addresses/active")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<AddressResponse> getActiveShippingAddress(
            @AuthenticationPrincipal String customerIdPrincipal
    ) {
        return ResponseEntity.ok(customerService.getActiveShippingAddress(customerIdPrincipal));
    }

    @GetMapping
    @PreAuthorize("hasRole('BACKOFFICE')")
    public ResponseEntity<List<CustomerSummaryResponse>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @GetMapping("/exists/{customerId}")
    @PreAuthorize("hasRole('BACKOFFICE')")
    public ResponseEntity<Boolean> checkCustomerById(
            @PathVariable("customerId") String customerId
    ) {
        return ResponseEntity.ok(customerService.checkCustomerById(customerId));
    }

    @GetMapping("/{customerId}")
    @PreAuthorize("hasRole('BACKOFFICE')")
    public ResponseEntity<CustomerDetailResponse> getCustomerById(
            @PathVariable("customerId") String customerId
    ) {
        return ResponseEntity.ok(customerService.getProfile(customerId));
    }

    @GetMapping("/{customerId}/billing-addresses")
    @PreAuthorize("hasRole('BACKOFFICE')")
    public ResponseEntity<List<AddressResponse>> getBillingAddressesByCustomerId(
            @PathVariable("customerId") String customerId
    ) {
        return ResponseEntity.ok(customerService.getBillingAddresses(customerId));
    }

    @GetMapping("/{customerId}/shipping-addresses")
    @PreAuthorize("hasRole('BACKOFFICE')")
    public ResponseEntity<List<AddressResponse>> getShippingAddressesByCustomerId(
            @PathVariable("customerId") String customerId
    ) {
        return ResponseEntity.ok(customerService.getShippingAddresses(customerId));
    }
}