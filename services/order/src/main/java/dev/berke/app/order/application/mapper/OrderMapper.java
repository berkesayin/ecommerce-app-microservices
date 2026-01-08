package dev.berke.app.order.application.mapper;

import dev.berke.app.order.domain.model.Order;
import dev.berke.app.order.api.dto.OrderRequest;
import dev.berke.app.order.api.dto.OrderResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

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

}