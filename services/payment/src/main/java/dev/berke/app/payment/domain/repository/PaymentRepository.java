package dev.berke.app.payment.domain.repository;

import dev.berke.app.payment.domain.model.CreditCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<CreditCard, Integer> {

    List<CreditCard> findByCustomerId(String customerId);

    boolean existsByCardNumberAndCustomerId(String cardNumber, String customerId);
}
