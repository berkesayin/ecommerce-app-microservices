package dev.berke.app.payment.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record PaymentDetailResponse(
        // transaction summary
        String paymentId,
        String status,
        String paymentStatus,
        String phase,
        Long systemTime,

        // bank and security
        String authCode,
        String hostReference,
        String connectorName,
        Integer fraudStatus,
        Integer mdStatus,

        // basket and price
        BigDecimal price,
        BigDecimal paidPrice,
        String currency,
        Integer installment,
        String basketId,

        // using other records
        PaymentCardDetail cardDetail,
        PaymentCommissionDetail commissionDetail,
        List<PaymentItemDetail> items,
        String errorMessage
) {
}