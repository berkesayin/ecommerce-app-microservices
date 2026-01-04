package dev.berke.app.payment.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.CreditCardNumber;

public record CreditCardRequest(
        @Pattern(regexp = "^[a-zA-Z\\s]*$", message = "Card holder name must contain only letters")
        @NotBlank(message = "Card holder name is required")
        @Size(min = 2, max = 100, message = "Card holder name must be between 2 and 100 characters")
        String cardHolderName,

        @Pattern(regexp = "^[0-9]{13,19}$", message = "Card number must be between 13 and 19 digits")
        @NotBlank(message = "Card number is required")
        @CreditCardNumber(message = "Invalid credit card number checksum")
        String cardNumber,

        @Pattern(regexp = "^(0[1-9]|1[0-2])$", message = "Expire month must be two digits (01-12)")
        @NotBlank(message = "Expiration month is required")
        String expireMonth,

        @Pattern(regexp = "^20[0-9]{2}$", message = "Expire year must be 4 digits (e.g. 2025)")
        @NotBlank(message = "Expiration year is required")
        String expireYear,

        @NotBlank(message = "CVC is required")
        @Pattern(regexp = "^[0-9]{3,4}$", message = "CVC must be 3 or 4 digits")
        String cvc
) {
}