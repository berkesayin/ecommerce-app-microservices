package dev.berke.app.payment.api.dto;

public record CreditCardResponse(
        Integer id,
        String customerId,
        String cardHolderName,
        String cardNumber,
        String expireMonth,
        String expireYear
) {
}