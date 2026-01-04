package dev.berke.app.payment.api.dto;

public record PaymentCardDetail(
        String cardHolderName,
        String binNumber,
        String lastFourDigits,
        String cardType,
        String cardAssociation,
        String cardFamily,
        String bankName
) {
}