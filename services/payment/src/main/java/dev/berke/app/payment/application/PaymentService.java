package dev.berke.app.payment.application;

import dev.berke.app.payment.domain.model.CreditCard;
import dev.berke.app.payment.application.mapper.PaymentMapper;
import dev.berke.app.payment.domain.repository.PaymentRepository;
import dev.berke.app.payment.api.dto.CreditCardRequest;
import dev.berke.app.payment.api.dto.CreditCardResponse;
import dev.berke.app.shared.exception.CreditCardNotFoundException;
import dev.berke.app.shared.exception.InvalidRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    @Transactional
    public CreditCardResponse createCreditCard(
            CreditCardRequest creditCardRequest,
            String customerId
    ) {
        if (paymentRepository.existsByCardNumberAndCustomerId(creditCardRequest.cardNumber(), customerId)) {
            throw new InvalidRequestException("This credit card is already saved.");
        }

        var creditCard = paymentMapper.toCreditCard(creditCardRequest, customerId);
        var savedCard = paymentRepository.save(creditCard);

        log.info("Credit card saved for customer ID: {}", customerId);

        return paymentMapper.toCreditCardResponse(savedCard);
    }

    @Transactional(readOnly = true)
    public List<CreditCardResponse> getCreditCards(String customerId) {
        log.info("Fetching credit cards for customer ID: {}", customerId);

        List<CreditCard> creditCards = paymentRepository.findByCustomerId(customerId);

        if (creditCards.isEmpty()) {
            log.info("No credit cards found for customer ID: {}", customerId);
            return Collections.emptyList();
        }

        log.info("Found {} credit cards for customer ID: {}", creditCards.size(), customerId);
        return paymentMapper.toCreditCardResponseList(creditCards);
    }

    @Transactional(readOnly = true)
    public CreditCard getProcessableCreditCard(String customerId) {
        List<CreditCard> creditCards = paymentRepository.findByCustomerId(customerId);

        if (creditCards.isEmpty()) {
            throw new CreditCardNotFoundException("No saved credit cards found for payment.");
        }

        return creditCards.get(0);
    }
}
