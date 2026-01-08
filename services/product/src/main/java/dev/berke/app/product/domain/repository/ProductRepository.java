package dev.berke.app.product.domain.repository;

import dev.berke.app.product.domain.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    boolean existsBySku(String sku);
}
