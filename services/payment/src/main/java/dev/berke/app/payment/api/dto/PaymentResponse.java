package dev.berke.app.payment.api.dto;

import java.math.BigDecimal;

public record PaymentResponse(
        String status,
        String paymentId,
        String conversationId,
        String errorMessage,
        BigDecimal paidPrice
) {
}