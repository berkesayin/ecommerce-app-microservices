package dev.berke.app.basket.domain.model;

import java.math.BigDecimal;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.springframework.util.Assert;

@Getter
@NoArgsConstructor
@EqualsAndHashCode(of = "productId")
public class BasketItem {

    private Integer productId;
    private String productName;
    private BigDecimal basePrice;
    private String manufacturer;
    private Integer categoryId;
    private ItemType itemType;
    private Integer quantity;

    public BasketItem(
            Integer productId,
            String productName,
            BigDecimal basePrice,
            String manufacturer,
            Integer categoryId,
            ItemType itemType,
            Integer quantity
    ) {
        Assert.notNull(productId, "Product ID cannot be null");
        Assert.hasText(productName, "Product name cannot be empty");
        Assert.isTrue(basePrice != null && basePrice.compareTo(BigDecimal.ZERO) >= 0,
                "Price must be non-negative");

        Assert.isTrue(quantity != null && quantity > 0,
                "Quantity must be greater than zero");

        this.productId = productId;
        this.productName = productName;
        this.basePrice = basePrice;
        this.manufacturer = manufacturer;
        this.categoryId = categoryId;
        this.itemType = itemType;
        this.quantity = quantity;
    }

    // DDD, business

    public void increaseQuantity(Integer amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Increase amount must be positive");
        }
        this.quantity += amount;
    }

    public void setQuantity(Integer newQuantity) {
        if (newQuantity == null || newQuantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        this.quantity = newQuantity;
    }

    public BigDecimal calculateLineTotal() {
        if (basePrice == null || quantity == null) return BigDecimal.ZERO;
        return basePrice.multiply(BigDecimal.valueOf(quantity));
    }
}