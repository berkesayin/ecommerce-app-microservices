package dev.berke.app.payment.infrastructure.paymentprovider.iyzipay;

import com.iyzipay.Options;
import com.iyzipay.model.Address;
import com.iyzipay.model.BasketItem;
import com.iyzipay.model.BasketItemType;
import com.iyzipay.model.Buyer;
import com.iyzipay.model.Payment;
import com.iyzipay.model.PaymentCard;
import com.iyzipay.request.CreatePaymentRequest;

import dev.berke.app.payment.infrastructure.client.basket.BasketClient;
import dev.berke.app.payment.infrastructure.client.basket.BasketResponse;
import dev.berke.app.payment.infrastructure.client.basket.BasketTotalPriceResponse;
import dev.berke.app.payment.api.dto.CreditCardResponse;
import dev.berke.app.payment.infrastructure.client.customer.CustomerClient;
import dev.berke.app.payment.infrastructure.messaging.PaymentEventProducer;
import dev.berke.app.payment.domain.event.PaymentReceivedEvent;
import dev.berke.app.payment.PaymentMethod;
import dev.berke.app.payment.api.dto.PaymentResponse;
import dev.berke.app.payment.application.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.List;
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

    public PaymentResponse createPaymentRequestWithCard(
            String customerId
    ) {
        // creating and setting buyer info for iyzipayment
        // paymentCard, buyer, billingAddress, shippingAddress, basketItems, totalBasketPrice

        // for calls within the service, passing customerId explicitly

        // for calls to other services with feign requests, customerId is not explicitly sent
        // instead, FeignClientInterceptor propagates the user's JWT

        CreatePaymentRequest request = new CreatePaymentRequest();

        PaymentCard paymentCard = createPaymentCard(customerId);
        request.setPaymentCard(paymentCard);

        Buyer buyer = createBuyer();
        request.setBuyer(buyer);

        Address billingAddress = createBillingAddress();
        request.setBillingAddress(billingAddress);

        Address shippingAddress = createShippingAddress();
        request.setShippingAddress(shippingAddress);

        List<BasketItem> basketItems = createBasketItems();
        request.setBasketItems(basketItems);

        BigDecimal totalBasketPrice = calculateTotalBasketPrice();
        request.setPrice(totalBasketPrice);
        request.setPaidPrice(totalBasketPrice);

        // log.info("Payment Request: {}", request);

        log.info("Initiating iyzico payment. CustomerId: {}, Price: {}, BasketItemCount: {}",
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

        var customerName = request.getBuyer().getName() + " " + request.getBuyer().getSurname();
        var paymentMethod = PaymentMethod.IYZICO_PAYMENT;

        paymentEventProducer.sendPaymentNotification(
                new PaymentReceivedEvent(
                        customerName,
                        request.getBuyer().getEmail(),
                        totalBasketPrice,
                        paymentMethod
                )
        );

        return new PaymentResponse(payment.getPaymentStatus(), payment.getPaymentId());
    }

    private Buyer createBuyer() {
        Buyer buyer = new Buyer();

        var customer = this.customerClient.getProfile()
                .orElseThrow(() -> new RuntimeException(
                        "Customer not found"));

        var activeShippingAddress = customerClient.getActiveShippingAddress();

        buyer.setId(customer.id());
        buyer.setName(customer.name());
        buyer.setSurname(customer.surname());
        buyer.setGsmNumber(customer.gsmNumber().toString());
        buyer.setEmail(customer.email());
        buyer.setIdentityNumber(customer.identityNumber());
        buyer.setRegistrationAddress(customer.registrationAddress().toString());
        buyer.setCity(activeShippingAddress.city());
        buyer.setCountry(activeShippingAddress.country());
        buyer.setZipCode(activeShippingAddress.zipCode());

        return buyer;
    }

    private Address createBillingAddress() {
        Address billingAddress = new Address();

        var activeBillingAddress = customerClient.getActiveBillingAddress();

        billingAddress.setContactName(activeBillingAddress.contactName());
        billingAddress.setCity(activeBillingAddress.city());
        billingAddress.setCountry(activeBillingAddress.country());
        billingAddress.setAddress(activeBillingAddress.address());
        billingAddress.setZipCode(activeBillingAddress.zipCode());

        return billingAddress;
    }

    private Address createShippingAddress() {
        Address shippingAddress = new Address();

        var activeShippingAddress = customerClient.getActiveBillingAddress();

        shippingAddress.setContactName(activeShippingAddress.contactName());
        shippingAddress.setCity(activeShippingAddress.city());
        shippingAddress.setCountry(activeShippingAddress.country());
        shippingAddress.setAddress(activeShippingAddress.address());
        shippingAddress.setZipCode(activeShippingAddress.zipCode());

        return shippingAddress;
    }

    private PaymentCard createPaymentCard(String customerId) {
        List<CreditCardResponse> creditCards =
                paymentService.getCreditCards(customerId);

        if (creditCards != null && !creditCards.isEmpty()) {
            CreditCardResponse creditCardResponse = creditCards.get(0);

            PaymentCard paymentCard = new PaymentCard();

            paymentCard.setCardHolderName(creditCardResponse.cardHolderName());
            paymentCard.setCardNumber(creditCardResponse.cardNumber());
            paymentCard.setExpireMonth(creditCardResponse.expireMonth());
            paymentCard.setExpireYear(creditCardResponse.expireYear());
            paymentCard.setCvc(creditCardResponse.cvc());

            return paymentCard;
        } else {
            log.warn("No credit cards found for customer: {}", customerId);
            return new PaymentCard();
        }
    }

    private List<com.iyzipay.model.BasketItem> createBasketItems() {
        BasketResponse basketResponse = basketClient.getBasket();
        List<dev.berke.app.payment.infrastructure.client.basket.BasketItem> fetchedBasketItems =
                basketResponse.items();

        List<com.iyzipay.model.BasketItem> basketItems = fetchedBasketItems.stream()
                .map(item -> {
                    com.iyzipay.model.BasketItem iyziBasketItem = new com.iyzipay.model.BasketItem();
                    iyziBasketItem.setId(String.valueOf(item.getProductId()));
                    iyziBasketItem.setName(item.getProductName());
                    iyziBasketItem.setCategory1(String.valueOf(item.getCategoryId()));
                    iyziBasketItem.setItemType(BasketItemType.PHYSICAL.name());
                    iyziBasketItem.setPrice(item
                            .getBasePrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity())
                            ));
                    return iyziBasketItem;
                })
                .collect(Collectors.toList());
        return basketItems;
    }

    private BigDecimal calculateTotalBasketPrice() {
        BasketTotalPriceResponse totalPriceResponse =
                basketClient.getTotalBasketPrice();

        return totalPriceResponse.totalPrice();
    }

}
