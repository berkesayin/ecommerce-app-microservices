package dev.berke.app.payment.domain.repository;

import dev.berke.app.payment.domain.model.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    List<PaymentTransaction> findByCustomerId(String customerId);

    Optional<PaymentTransaction> findByConversationId(String conversationId);

    Optional<PaymentTransaction> findByPaymentId(String paymentId);
}