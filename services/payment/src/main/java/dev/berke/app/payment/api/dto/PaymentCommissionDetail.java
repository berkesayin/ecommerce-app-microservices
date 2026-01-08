package dev.berke.app.payment.api.dto;

import java.math.BigDecimal;

public record PaymentCommissionDetail(
        BigDecimal merchantCommissionRate,
        BigDecimal merchantCommissionRateAmount,
        BigDecimal iyziCommissionRateAmount,
        BigDecimal iyziCommissionFee
) {
}