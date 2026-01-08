package dev.berke.app.orderline.application.mapper;

import dev.berke.app.order.domain.model.Order;
import dev.berke.app.orderline.domain.model.Orderline;
import dev.berke.app.orderline.api.dto.OrderlineRequest;
import dev.berke.app.orderline.api.dto.OrderlineResponse;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
public class OrderlineMapper {

    public Orderline toOrderLine(OrderlineRequest orderlineRequest) {
        return Orderline.builder()
                .id(orderlineRequest.id())
                .order(
                        Order.builder()
                                .id(orderlineRequest.orderId())
                                .build()
                )
                .productId(orderlineRequest.productId())
                .quantity(orderlineRequest.quantity())
                .build();
    }

    public OrderlineResponse toOrderLineResponse(Orderline orderline) {
        return new OrderlineResponse(orderline.getId(), orderline.getQuantity());
    }
}