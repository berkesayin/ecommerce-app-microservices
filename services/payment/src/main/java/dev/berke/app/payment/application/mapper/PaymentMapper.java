package dev.berke.app.payment.application.mapper;

import com.iyzipay.model.Payment;
import com.iyzipay.model.PaymentItem;
import dev.berke.app.payment.api.dto.CreditCardRequest;
import dev.berke.app.payment.api.dto.CreditCardResponse;
import dev.berke.app.payment.api.dto.PaymentCardDetail;
import dev.berke.app.payment.api.dto.PaymentCommissionDetail;
import dev.berke.app.payment.api.dto.PaymentDetailResponse;
import dev.berke.app.payment.api.dto.PaymentItemDetail;
import dev.berke.app.payment.domain.model.CreditCard;
import dev.berke.app.payment.domain.model.PaymentTransaction;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PaymentMapper {

    public CreditCard toCreditCard(CreditCardRequest creditCardRequest, String customerId) {
        return CreditCard.builder()
                .customerId(customerId)
                .cardHolderName(creditCardRequest.cardHolderName())
                .cardNumber(creditCardRequest.cardNumber())
                .expireMonth(creditCardRequest.expireMonth())
                .expireYear(creditCardRequest.expireYear())
                .cvc(creditCardRequest.cvc())
                .build();
    }

    public CreditCardResponse toCreditCardResponse(CreditCard creditCard) {
        return new CreditCardResponse(
                creditCard.getId(),
                creditCard.getCustomerId(),
                creditCard.getCardHolderName(),
                maskCardNumber(creditCard.getCardNumber()),
                creditCard.getExpireMonth(),
                creditCard.getExpireYear()
        );
    }

    public List<CreditCardResponse> toCreditCardResponseList(List<CreditCard> creditCards) {
        if (creditCards == null) return List.of();
        return creditCards.stream()
                .map(this::toCreditCardResponse)
                .collect(Collectors.toList());
    }

    public PaymentTransaction toPaymentTransaction(
            Payment payment,
            String customerId,
            BigDecimal requestPrice
    ) {
        return PaymentTransaction.builder()
                .paymentId(payment.getPaymentId())
                .conversationId(payment.getConversationId())
                .customerId(customerId)
                .status(payment.getStatus())
                .errorCode(payment.getErrorCode())
                .errorMessage(payment.getErrorMessage())
                .price(requestPrice)
                .paidPrice(payment.getPaidPrice())
                .currency(payment.getCurrency())
                .build();
    }

    public PaymentDetailResponse toPaymentDetailResponse(Payment payment) {
        PaymentCardDetail cardDetail = new PaymentCardDetail(
                null,
                payment.getBinNumber(),
                payment.getLastFourDigits(),
                payment.getCardType(),
                payment.getCardAssociation(),
                payment.getCardFamily(),
                payment.getBankName()
        );

        PaymentCommissionDetail commissionDetail = new PaymentCommissionDetail(
                payment.getMerchantCommissionRate(),
                payment.getMerchantCommissionRateAmount(),
                payment.getIyziCommissionRateAmount(),
                payment.getIyziCommissionFee()
        );

        List<PaymentItemDetail> itemDetails = toPaymentItemDetailList(payment.getPaymentItems());

        return new PaymentDetailResponse(
                // transaction
                payment.getPaymentId(),
                payment.getStatus(),
                payment.getPaymentStatus(),
                payment.getPhase(),
                payment.getSystemTime(),

                // bank and security
                payment.getAuthCode(),
                payment.getHostReference(),
                payment.getConnectorName(),
                payment.getFraudStatus(),
                payment.getMdStatus(),

                // basket and price
                payment.getPrice(),
                payment.getPaidPrice(),
                payment.getCurrency(),
                payment.getInstallment(),
                payment.getBasketId(),

                cardDetail,
                commissionDetail,
                itemDetails,

                payment.getErrorMessage()
        );
    }

    private String maskCardNumber(String source) {
        if (source == null || source.length() < 4) {
            return "****";
        }
        return "************" + source.substring(source.length() - 4);
    }

    private List<PaymentItemDetail> toPaymentItemDetailList(List<PaymentItem> items) {
        if (items == null) {
            return Collections.emptyList();
        }
        return items.stream()
                .map(item -> new PaymentItemDetail(
                        item.getItemId(),
                        item.getPaymentTransactionId(),
                        item.getPrice(),
                        item.getPaidPrice(),
                        item.getTransactionStatus()
                ))
                .collect(Collectors.toList());
    }
}