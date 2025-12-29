package dev.berke.app.product.domain.model;

import dev.berke.app.category.domain.model.Category;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
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

    @Builder.Default
    @Column(name = "status")
    private Boolean status = false;

    @ManyToOne(optional = false) // a product must have a category
    @JoinColumn(
            name = "category_id", // foreign key at products
            referencedColumnName = "category_id", // primary key at categories
            nullable = false
    )
    private Category category;

    @PrePersist
    protected void onCreate() {
        if (createdOn == null) {
            createdOn = Instant.now();
        }

        if (status == null) {
            status = false;
        }
    }

    public boolean isActive() {
        return Boolean.TRUE.equals(this.status);
    }
}