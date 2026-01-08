package dev.berke.app.payment.infrastructure.paymentprovider.iyzipay;

import com.iyzipay.Options;
import com.iyzipay.model.*;
import com.iyzipay.request.CreatePaymentRequest;

import com.iyzipay.request.RetrievePaymentRequest;
import dev.berke.app.payment.api.dto.PaymentDetailResponse;
import dev.berke.app.payment.application.mapper.PaymentMapper;
import dev.berke.app.payment.domain.model.CreditCard;
import dev.berke.app.payment.domain.model.PaymentTransaction;
import dev.berke.app.payment.domain.repository.PaymentTransactionRepository;
import dev.berke.app.payment.infrastructure.client.basket.BasketClient;
import dev.berke.app.payment.infrastructure.client.basket.BasketResponse;
import dev.berke.app.payment.infrastructure.client.basket.BasketTotalPriceResponse;
import dev.berke.app.payment.infrastructure.client.customer.CustomerClient;
import dev.berke.app.payment.infrastructure.messaging.PaymentEventProducer;
import dev.berke.app.payment.domain.event.PaymentReceivedEvent;
import dev.berke.app.payment.PaymentMethod;
import dev.berke.app.payment.api.dto.PaymentResponse;
import dev.berke.app.payment.application.PaymentService;

import dev.berke.app.shared.exception.InvalidRequestException;
import dev.berke.app.shared.exception.PaymentExecutionException;
import dev.berke.app.shared.exception.UpstreamDataException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class IyzipayService {

    private final Options useIyzipayOptions;
    private final CustomerClient customerClient;
    private final BasketClient basketClient;
    private final PaymentService paymentService;
    private final PaymentEventProducer paymentEventProducer;
    private final PaymentMapper paymentMapper;
    private final PaymentTransactionRepository paymentTransactionRepository;

    public PaymentResponse createPayment(
            String customerId
    ) {
        // creating and setting buyer info for iyzipayment
        // paymentCard, buyer, billingAddress, shippingAddress, basketItems, totalBasketPrice

        // for calls within the service, passing customerId explicitly

        // for calls to other services with feign requests, customerId is not explicitly sent
        // instead, FeignClientInterceptor propagates the user's JWT

        CreatePaymentRequest request = new CreatePaymentRequest();

        String conversationId = UUID.randomUUID().toString();
        request.setConversationId(conversationId);

        List<BasketItem> basketItems = createBasketItems();
        if (basketItems.isEmpty()) {
            throw new InvalidRequestException("Cannot process payment for an empty basket.");
        }
        request.setBasketItems(basketItems);

        BigDecimal totalBasketPrice = calculateTotalBasketPrice();
        if (totalBasketPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidRequestException("Total price must be greater than zero.");
        }

        request.setPrice(totalBasketPrice);
        request.setPaidPrice(totalBasketPrice);

        Buyer buyer = createBuyer();
        request.setBuyer(buyer);

        Address billingAddress = createBillingAddress();
        request.setBillingAddress(billingAddress);

        Address shippingAddress = createShippingAddress();
        request.setShippingAddress(shippingAddress);

        PaymentCard paymentCard = createPaymentCard(customerId);
        request.setPaymentCard(paymentCard);

        log.info("Initiating iyzico payment. ConversationId: {}, CustomerId: {}, Price: {}, BasketItemCount: {}",
                conversationId,
                customerId,
                totalBasketPrice,
                basketItems.size()
        );

        if (log.isDebugEnabled()) {
            log.debug("Payment Details - Buyer: {}, ShippingCity: {}, BillingCity: {}",
                    buyer.getEmail(),
                    shippingAddress.getCity(),
                    billingAddress.getCity()
            );
        }

        // create payment using the injected options
        Payment payment = Payment.create(request, useIyzipayOptions);
        log.info("Iyzico Payment response received. ConversationID: {}, Status: {}, PaymentId: {}",
                payment.getConversationId(), payment.getStatus(), payment.getPaymentId());

        savePaymentTransaction(payment, customerId, totalBasketPrice);

        // check payment status
        if (!"success".equalsIgnoreCase(payment.getStatus())) {
            log.error("CreditCard failed. ErrorCode: {}, ErrorMessage: {}",
                    payment.getErrorCode(), payment.getErrorMessage());

            throw new PaymentExecutionException(
                    String.format("CreditCard failed: %s", payment.getErrorMessage())
            );
        }

        // payment received
        var customerName = buyer.getName() + " " + buyer.getSurname();
        paymentEventProducer.sendPaymentNotification(
                new PaymentReceivedEvent(
                        customerName,
                        buyer.getEmail(),
                        totalBasketPrice,
                        PaymentMethod.IYZICO_PAYMENT
                )
        );

        return new PaymentResponse(
                payment.getStatus(),
                payment.getPaymentId(),
                payment.getConversationId(),
                payment.getErrorMessage(), // null if payment received
                payment.getPaidPrice()
        );
    }

    private Buyer createBuyer() {
        Buyer buyer = new Buyer();

        var customer = this.customerClient.getProfile()
                .orElseThrow(() -> new UpstreamDataException(
                        "Cannot retrieve customer profile for payment.")
                );

        var activeShippingAddress = customerClient.getActiveShippingAddress();
        if (activeShippingAddress == null) {
            throw new UpstreamDataException("Active shipping address not found in customer service.");
        }

        buyer.setId(customer.id());
        buyer.setName(customer.name());
        buyer.setSurname(customer.surname());
        buyer.setGsmNumber(customer.gsmNumber());
        buyer.setEmail(customer.email());
        buyer.setIdentityNumber(customer.identityNumber());
        buyer.setRegistrationAddress(customer.registrationAddress());
        buyer.setCity(activeShippingAddress.city());
        buyer.setCountry(activeShippingAddress.country());
        buyer.setZipCode(activeShippingAddress.zipCode());

        return buyer;
    }

    private Address createBillingAddress() {
        Address billingAddress = new Address();
        var activeBillingAddress = customerClient.getActiveBillingAddress();

        if (activeBillingAddress == null) {
            throw new UpstreamDataException("Active billing address not found in customer service.");
        }

        billingAddress.setContactName(activeBillingAddress.contactName());
        billingAddress.setCity(activeBillingAddress.city());
        billingAddress.setCountry(activeBillingAddress.country());
        billingAddress.setAddress(activeBillingAddress.address());
        billingAddress.setZipCode(activeBillingAddress.zipCode());

        return billingAddress;
    }

    private Address createShippingAddress() {
        Address shippingAddress = new Address();
        var activeShippingAddress = customerClient.getActiveShippingAddress();

        if (activeShippingAddress == null) {
            throw new UpstreamDataException("Active shipping address not found in customer service.");
        }

        shippingAddress.setContactName(activeShippingAddress.contactName());
        shippingAddress.setCity(activeShippingAddress.city());
        shippingAddress.setCountry(activeShippingAddress.country());
        shippingAddress.setAddress(activeShippingAddress.address());
        shippingAddress.setZipCode(activeShippingAddress.zipCode());

        return shippingAddress;
    }

    private PaymentCard createPaymentCard(String customerId) {
        CreditCard creditCard = paymentService.getProcessableCreditCard(customerId);

        PaymentCard paymentCard = new PaymentCard();

        paymentCard.setCardHolderName(creditCard.getCardHolderName());
        paymentCard.setCardNumber(creditCard.getCardNumber());
        paymentCard.setExpireMonth(creditCard.getExpireMonth());
        paymentCard.setExpireYear(creditCard.getExpireYear());
        paymentCard.setCvc(creditCard.getCvc());

        return paymentCard;
    }

    private List<com.iyzipay.model.BasketItem> createBasketItems() {
        BasketResponse basketResponse = basketClient.getBasket();

        if (basketResponse == null || basketResponse.items() == null) {
            throw new UpstreamDataException("Cannot retrieve basket items.");
        }

        return basketResponse.items().stream()
                .map(item -> {
                    com.iyzipay.model.BasketItem iyziBasketItem = new com.iyzipay.model.BasketItem();
                    iyziBasketItem.setId(String.valueOf(item.getProductId()));
                    iyziBasketItem.setName(item.getProductName());
                    iyziBasketItem.setCategory1(String.valueOf(item.getCategoryId()));
                    iyziBasketItem.setItemType(BasketItemType.PHYSICAL.name());
                    iyziBasketItem.setPrice(item
                            .getBasePrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity()))
                    );
                    return iyziBasketItem;
                })
                .collect(Collectors.toList());
    }

    private BigDecimal calculateTotalBasketPrice() {
        BasketTotalPriceResponse totalPriceResponse = basketClient.getTotalBasketPrice();
        if (totalPriceResponse == null || totalPriceResponse.totalPrice() == null) {
            throw new UpstreamDataException("Cannot to retrieve basket price.");
        }

        return totalPriceResponse.totalPrice();
    }

    private void savePaymentTransaction(
            Payment payment,
            String customerId,
            BigDecimal requestPrice
    ) {
        try {
            PaymentTransaction transaction =
                    paymentMapper.toPaymentTransaction(payment, customerId, requestPrice);

            paymentTransactionRepository.save(transaction);

        } catch (Exception e) {
            // if the db save fails (connection timeout etc.), but bank payment is successful
            // error is just logged
            log.error("CRITICAL: Failed to save PaymentTransaction to DB! PaymentId: {}",
                    payment.getPaymentId(), e);
        }
    }

    public PaymentDetailResponse getPaymentDetails(String paymentId) {
        RetrievePaymentRequest request = new RetrievePaymentRequest();
        request.setPaymentId(paymentId);

        log.info("Backoffice: Retrieving detailed payment info for Payment ID: {}", paymentId);

        Payment payment = Payment.retrieve(request, useIyzipayOptions);

        if (payment.getStatus() == null) {
            log.error("Iyzico API returned null status for Payment ID: {}", paymentId);
            throw new PaymentExecutionException("Cannot retrieve payment data.");
        }

        return paymentMapper.toPaymentDetailResponse(payment);
    }
}