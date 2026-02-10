package dev.berke.app.product.domain.model;

import dev.berke.app.category.domain.model.Category;
import dev.berke.app.shared.exception.InvalidDomainStateException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "products")
@EqualsAndHashCode(of = {"productId"})
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_seq")
    @SequenceGenerator(name = "product_seq", sequenceName = "product_seq", allocationSize = 1)
    @Column(name = "product_id", nullable = false, updatable = false)
    private Integer productId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "base_price", precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "min_price", precision = 10, scale = 2)
    private BigDecimal minPrice;

    @Column(name = "manufacturer")
    private String manufacturer;

    @Column(name = "sku", unique = true, nullable = false)
    private String sku;

    @Column(name = "created_on", nullable = false, updatable = false)
    private Instant createdOn;

    @Column(name = "status")
    private Boolean status;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // DDD with constructor and business methods

    // constructor: valid state after creations
    public Product(
            String productName,
            BigDecimal basePrice,
            BigDecimal minPrice,
            String manufacturer,
            String sku,
            Category category
    ) {
        validateProductDetails(productName, basePrice, minPrice, sku);
        Assert.notNull(category, "Category cannot be null");

        this.productName = productName;
        this.basePrice = basePrice;
        this.minPrice = minPrice;
        this.manufacturer = manufacturer;
        this.sku = sku;
        this.category = category;
        this.createdOn = Instant.now();
        this.status = false;
    }

    public void publish() {
        if (this.status) {
            throw new InvalidDomainStateException("Product is already published");
        }
        this.status = true;
    }

    public void unpublish() {
        if (!this.status) {
            throw new InvalidDomainStateException("Product is already unpublished");
        }
        this.status = false;
    }

    public void updateDetails(
            String productName,
            BigDecimal basePrice,
            BigDecimal minPrice
    ) {
        validateProductDetails(productName, basePrice, minPrice, sku);

        this.productName = productName;
        this.basePrice = basePrice;
        this.minPrice = minPrice;
    }

    public void changeCategory (Category newCategory) {
        Assert.notNull(newCategory, "New category cannot be null");
        this.category = newCategory;
    }

    private void validateProductDetails(
            String productName,
            BigDecimal basePrice,
            BigDecimal minPrice,
            String sku
    ) {
        Assert.hasText(productName, "Product name is required");
        Assert.notNull(basePrice, "Base price is required");
        Assert.hasText(sku, "SKU is required");
        Assert.notNull(minPrice, "Min price is required");

        if (basePrice.compareTo(BigDecimal.ZERO) < 0 || minPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Prices cannot be negative");
        }
        if (basePrice.compareTo(minPrice) < 0) {
            throw new IllegalArgumentException("Base price cannot be lower than minimum price");
        }
    }
}