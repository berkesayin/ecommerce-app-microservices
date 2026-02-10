package dev.berke.app.product.application.mapper;

import dev.berke.app.category.domain.model.Category;
import dev.berke.app.product.domain.event.ProductPublishedEvent;
import dev.berke.app.product.domain.model.Product;
import dev.berke.app.product.api.dto.ProductCreateRequest;
import dev.berke.app.product.api.dto.ProductResponse;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toProduct(ProductCreateRequest productCreateRequest, Category category) {
        if (productCreateRequest == null) return null;

        return new Product(
                productCreateRequest.productName(),
                productCreateRequest.basePrice(),
                productCreateRequest.minPrice(),
                productCreateRequest.manufacturer(),
                productCreateRequest.sku(),
                category
        );
    }

    public ProductResponse toProductResponse(Product product) {
        if (product == null) return null;

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

    public ProductPublishedEvent toProductPublishedEvent(Product product) {
        if (product == null) return null;

        return new ProductPublishedEvent(
                product.getProductId(),
                product.getProductName(),
                product.getCategory().getCategoryId(),
                product.getCategory().getCategoryName(),
                product.getBasePrice(),
                product.getMinPrice(),
                product.getManufacturer(),
                product.getSku(),
                true,
                product.getCreatedOn()
        );
    }
}
