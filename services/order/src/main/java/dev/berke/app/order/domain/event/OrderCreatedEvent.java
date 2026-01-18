package dev.berke.app.order.domain.event;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Builder
public record OrderCreatedEvent(
        String orderId,
        String reference,
        Instant orderDate,
        String status,
        BigDecimal totalAmount,
        String paymentMethod,
        CustomerInfo customer,
        AddressInfo shippingAddress,
        AddressInfo billingAddress,
        List<ItemInfo> items
) {
    // denormalization for order index documents
    public record CustomerInfo(
            String id,
            String fullName,
            String email
    ) {}

    public record AddressInfo(
            String contactName,
            String city,
            String country,
            String address,
            String zipCode
    ) {}

    public record ItemInfo(
            Integer productId,
            String productName,
            String manufacturer,
            Integer categoryId,
            Integer quantity,
            BigDecimal basePrice
    ) {}
}