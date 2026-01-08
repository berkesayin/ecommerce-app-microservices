package dev.berke.app.payment.api.dto;

import java.math.BigDecimal;

public record PaymentItemDetail(
        String itemId,
        String paymentTransactionId,
        BigDecimal price,
        BigDecimal paidPrice,
        Integer transactionStatus
) {
}
