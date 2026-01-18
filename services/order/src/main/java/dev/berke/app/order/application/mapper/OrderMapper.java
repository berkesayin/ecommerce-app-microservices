package dev.berke.app.order.application.mapper;

import dev.berke.app.order.domain.event.OrderCreatedEvent;
import dev.berke.app.order.domain.model.Order;
import dev.berke.app.order.api.dto.OrderRequest;
import dev.berke.app.order.api.dto.OrderResponse;
import dev.berke.app.order.infrastructure.client.basket.BasketItem;
import dev.berke.app.order.infrastructure.client.customer.Address;
import dev.berke.app.order.infrastructure.client.customer.CustomerResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public Order toOrder(
            OrderRequest orderRequest,
            String customerId,
            String customerEmail,
            BigDecimal totalAmount
    ) {
        return Order.builder()
                .reference(orderRequest.reference())
                .customerId(customerId)
                .customerEmail(customerEmail)
                .totalAmount(totalAmount)
                .paymentMethod(orderRequest.paymentMethod())
                .build();
    }

    public OrderResponse fromOrder(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getReference()
        );
    }

    public OrderCreatedEvent toOrderCreatedEvent(
            Order order,
            CustomerResponse customer,
            Address shippingAddress,
            Address billingAddress,
            List<BasketItem> basketItems
    ) {
        return OrderCreatedEvent.builder()
                .orderId(order.getId().toString())
                .reference(order.getReference())
                .orderDate(order.getCreatedDate())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .paymentMethod(order.getPaymentMethod().name())
                .customer(new OrderCreatedEvent.CustomerInfo(
                        customer.id(),
                        customer.name() + " " + customer.surname(),
                        customer.email()
                ))
                .shippingAddress(toAddressInfo(shippingAddress))
                .billingAddress(toAddressInfo(billingAddress))
                .items(toItemInfos(basketItems))
                .build();
    }

    private OrderCreatedEvent.AddressInfo toAddressInfo(Address address) {
        return new OrderCreatedEvent.AddressInfo(
                address.contactName(),
                address.city(),
                address.country(),
                address.address(),
                address.zipCode()
        );
    }

    private List<OrderCreatedEvent.ItemInfo> toItemInfos(List<BasketItem> items) {
        return items.stream()
                .map(item -> new OrderCreatedEvent.ItemInfo(
                        item.productId(),
                        item.productName(),
                        item.manufacturer(),
                        item.categoryId(),
                        item.quantity(),
                        item.basePrice()
                ))
                .collect(Collectors.toList());
    }
}