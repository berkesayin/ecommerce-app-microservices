package dev.berke.app.basket.domain.model;

import dev.berke.app.shared.exception.BasketItemNotFoundException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
@NoArgsConstructor
@RedisHash("Basket")
public class Basket {

    @Id
    private String customerId;
    private List<BasketItem> items = new ArrayList<>();

    public Basket(String customerId) {
        this.customerId = customerId;
    }

    // DDD, business

    public void addItem(BasketItem newItem) {
        if (newItem == null) return;

        findItemByProductId(newItem.getProductId())
                .ifPresentOrElse(
                        existingItem -> existingItem.increaseQuantity(newItem.getQuantity()),
                        () -> this.items.add(newItem)
                );
    }

    public void changeQuantity(Integer productId, Integer newQuantity) {
        if (newQuantity == null || newQuantity <= 0) {
            removeProduct(productId);
            return;
        }

        BasketItem item = findItemByProductId(productId)
                .orElseThrow(() -> new BasketItemNotFoundException(
                        String.format(
                                "Cannot update quantity. Product not found in basket with ID: %s",
                                productId
                        )
                ));
        item.setQuantity(newQuantity);
    }

    public void removeProduct(Integer productId) {
        this.items.removeIf(item -> item.getProductId().equals(productId));
    }

    public void clearBasket() {
        this.items.clear();
    }

    public BigDecimal calculateTotalPrice() {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return items.stream()
                .map(BasketItem::calculateLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Optional<BasketItem> findItemByProductId(Integer productId) {
        return this.items.stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();
    }
}
