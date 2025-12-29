package dev.berke.app.product.application.mapper;

import dev.berke.app.category.domain.model.Category;
import dev.berke.app.product.domain.model.Product;
import dev.berke.app.product.api.dto.ProductCreateRequest;
import dev.berke.app.product.api.dto.ProductResponse;
import org.springframework.stereotype.Service;

@Service
public class ProductMapper {

    public Product toProduct(ProductCreateRequest productCreateRequest, Category category) {
        return Product.builder()
                .productName(productCreateRequest.productName())
                .basePrice(productCreateRequest.basePrice())
                .minPrice(productCreateRequest.minPrice())
                .manufacturer(productCreateRequest.manufacturer())
                .sku(productCreateRequest.sku())
                .status(false)
                .category(category)
                .build();
    }

    public ProductResponse toProductResponse(Product product) {
        return new ProductResponse(
                product.getProductId(),
                product.getProductName(),
                product.getBasePrice(),
                product.getMinPrice(),
                product.getManufacturer(),
                product.getSku(),
                product.getCreatedOn(),
                product.getStatus(),
                product.getCategory().getCategoryId()
        );
    }
}
