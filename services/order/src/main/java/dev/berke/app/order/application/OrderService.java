package dev.berke.app.order.application;

import dev.berke.app.order.infrastructure.client.basket.BasketResponse;
import dev.berke.app.order.infrastructure.client.customer.Address;
import dev.berke.app.order.infrastructure.client.customer.CustomerClient;
import dev.berke.app.order.infrastructure.client.customer.CustomerResponse;
import dev.berke.app.order.domain.event.OrderCreatedEvent;
import dev.berke.app.order.domain.event.OrderReceivedEvent;
import dev.berke.app.order.infrastructure.messaging.OrderEventProducer;
import dev.berke.app.order.api.dto.OrderRequest;
import dev.berke.app.order.api.dto.OrderResponse;
import dev.berke.app.order.application.mapper.OrderMapper;
import dev.berke.app.order.domain.model.Order;
import dev.berke.app.order.domain.model.OrderStatus;
import dev.berke.app.order.domain.repository.OrderRepository;
import dev.berke.app.order.infrastructure.client.basket.BasketClient;
import dev.berke.app.order.infrastructure.client.basket.BasketItem;
import dev.berke.app.orderline.api.dto.OrderlineRequest;
import dev.berke.app.orderline.application.OrderlineService;
import dev.berke.app.order.infrastructure.client.payment.PaymentClient;
import dev.berke.app.shared.exception.ExternalServiceException;
import dev.berke.app.shared.exception.InvalidOrderRequestException;
import dev.berke.app.shared.exception.OrderNotFoundException;
import dev.berke.app.shared.exception.PaymentProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderlineService orderLineService;
    private final OrderMapper orderMapper;
    private final CustomerClient customerClient;
    private final BasketClient basketClient;
    private final PaymentClient paymentClient;
    private final OrderEventProducer orderEventProducer;

    // Business logic to create order
    // 1. validate external data (customer and basket)
    // 2. create initial order with PENDING_PAYMENT status
    // 3. initiate payment with iyzi payment
    // 4. finalize order if payment received
    // 5. publish OrderCreatedEvent for order index (search)
    // 6. publish OrderReceivedEvent for customer email (notification)
    // 7. if payment failed, the order is saved to db with PAYMENT_FAILED and throws exception
    // 8. return order response

    @Transactional(noRollbackFor = PaymentProcessingException.class)
    public OrderResponse createOrder(OrderRequest orderRequest, String customerId) {

        // 1. validate external data
        CustomerResponse customer = validateAndGetCustomer(customerId);
        BasketResponse basket = validateAndGetBasket();
        BigDecimal totalPrice = calculateTotalPrice();

        log.info("Starting order process for customer: {} with total: {}", customer.id(), totalPrice);

        // 2. create initial order (Status: PENDING_PAYMENT)
        Order savedOrder = persistInitialOrder(orderRequest, customer, basket, totalPrice);

        // 3. initiate payment
        try {
            processPayment(savedOrder);

            finalizeOrder(savedOrder, customer, basket); // 4. if payment received

        } catch (PaymentProcessingException e) {
            handlePaymentError(savedOrder); // 7. if payment failed
            throw e;
        }

        return orderMapper.fromOrder(savedOrder);
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(orderMapper::fromOrder)
                .collect(Collectors.toList());
    }

    public OrderResponse getOrderById(Integer orderId) {
        return orderRepository.findById(orderId)
                .map(orderMapper::fromOrder)
                .orElseThrow(() -> new OrderNotFoundException(
                        String.format("Order not found with ID: %d", orderId)
                ));
    }

    private CustomerResponse validateAndGetCustomer(String customerId) {
        CustomerResponse customer = customerClient.getProfile()
                .orElseThrow(() -> new ExternalServiceException(
                        "Customer service is unavailable or returned empty response."));

        if (!customer.id().equals(customerId)) {
            throw new InvalidOrderRequestException(
                    "Authentication mismatch: Token does not match the requested customer profile.");
        }
        return customer;
    }

    private BasketResponse validateAndGetBasket() {
        BasketResponse basket = basketClient.getBasket()
                .orElseThrow(() -> new ExternalServiceException(
                        "Basket service is unavailable or returned empty response."));

        if (basket.items() == null || basket.items().isEmpty()) {
            throw new InvalidOrderRequestException("Cannot create an order with an empty basket.");
        }
        return basket;
    }

    private BigDecimal calculateTotalPrice() {
        try {
            var response = basketClient.getTotalBasketPrice();
            if (response == null || response.getBody() == null) {
                throw new ExternalServiceException("Cannot retrieve total price from Basket service.");
            }
            return response.getBody().totalPrice();
        } catch (Exception e) {
            throw new ExternalServiceException("Error communicating with Basket service: "
                    + e.getMessage());
        }
    }

    private Address findAddress(List<Address> addresses, String activeId, String type) {
        if (addresses == null || activeId == null) {
            throw new InvalidOrderRequestException(
                    String.format("Customer has no active %s address configured.", type));
        }

        return addresses.stream()
                .filter(a -> a.id().equals(activeId))
                .findFirst()
                .orElseThrow(() -> new InvalidOrderRequestException(
                        String.format("Active %s address (ID: %s) not found in customer profile.",
                                type, activeId)));
    }

    private Order persistInitialOrder(
            OrderRequest request,
            CustomerResponse customer,
            BasketResponse basket,
            BigDecimal totalPrice
    ) {
        Order order = orderMapper.toOrder(request, customer.id(), customer.email(), totalPrice);
        order.setStatus(OrderStatus.PENDING_PAYMENT);

        Order savedOrder = orderRepository.save(order);

        for (BasketItem basketItem : basket.items()) {
            orderLineService.saveOrderLine(new OrderlineRequest(
                    null,
                    savedOrder.getId(),
                    basketItem.productId(),
                    basketItem.quantity()
            ));
        }
        return savedOrder;
    }

    private void processPayment(Order order) {
        log.info("Initiating payment for Order Ref: {}", order.getReference());
        try {
            paymentClient.createPayment();
        } catch (Exception e) {
            log.error("Payment failed for Order Ref: {}", order.getReference(), e);
            throw new PaymentProcessingException("Payment gateway declined the transaction: "
                    + e.getMessage());
        }
    }

    private void handlePaymentError(Order order) {
        log.warn("Marking Order Ref: {} as PAYMENT_FAILED.", order.getReference());
        order.setStatus(OrderStatus.PAYMENT_FAILED);
        orderRepository.save(order);
    }

    private void finalizeOrder(Order order, CustomerResponse customer, BasketResponse basket) {
        order.setStatus(OrderStatus.PROCESSING);
        orderRepository.save(order);

        sendOrderEvents(order, customer, basket.items());
    }

    private void sendOrderEvents(Order order, CustomerResponse customer, List<BasketItem> items) {
        // 5. publish OrderCreatedEvent for order index (search)
        OrderCreatedEvent orderCreatedEvent = buildOrderCreatedEvent(order, customer, items);
        orderEventProducer.sendOrderCreated(orderCreatedEvent);

        // 6. publish OrderReceivedEvent for customer email (notification)
        orderEventProducer.sendOrderConfirmation(new OrderReceivedEvent(
                customer.name() + " " + customer.surname(),
                customer.email(),
                order.getReference(),
                order.getPaymentMethod(),
                items,
                order.getTotalAmount()
        ));
    }

    private OrderCreatedEvent buildOrderCreatedEvent(
            Order order,
            CustomerResponse customer,
            List<BasketItem> items
    ) {
        Address activeShippingAddress = findAddress(
                customer.shippingAddresses(),
                customer.activeShippingAddressId(),
                "shipping"
        );

        Address activeBillingAddress = findAddress(
                customer.billingAddresses(),
                customer.activeBillingAddressId(),
                "billing"
        );

        return orderMapper.toOrderCreatedEvent(
                order,
                customer,
                activeShippingAddress,
                activeBillingAddress,
                items
        );
    }

}