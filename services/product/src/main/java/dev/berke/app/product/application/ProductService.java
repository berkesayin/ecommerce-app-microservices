package dev.berke.app.product.application;

import dev.berke.app.category.domain.model.Category;
import dev.berke.app.category.domain.repository.CategoryRepository;
import dev.berke.app.product.domain.model.Product;
import dev.berke.app.product.infrastructure.messaging.ProductEventProducer;
import dev.berke.app.product.api.dto.ProductCreateRequest;
import dev.berke.app.product.api.dto.ProductResponse;
import dev.berke.app.product.application.mapper.ProductMapper;
import dev.berke.app.product.domain.repository.ProductRepository;
import dev.berke.app.shared.exception.CategoryNotFoundException;
import dev.berke.app.shared.exception.ProductAlreadyExistsException;
import dev.berke.app.shared.exception.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.InvalidRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final ProductEventProducer productEventProducer;

    public ProductResponse createProduct(ProductCreateRequest productCreateRequest) {
        if(productRepository.existsBySku(productCreateRequest.sku())) {
            throw new ProductAlreadyExistsException(
                    String.format("Product with this SKU code already exists: %s",
                            productCreateRequest.sku())
            );
        }

        Integer categoryId = productCreateRequest.categoryId();

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(
                        String.format("Category not found with ID: %s", categoryId)
                ));

        Product product = productMapper.toProduct(productCreateRequest, category);
        Product savedProduct = productRepository.save(product);

        return productMapper.toProductResponse(savedProduct);
    }

    @Transactional
    public ProductResponse setProductStatus(Integer productId, boolean newStatus) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(
                        String.format("Product not found with ID: %s", productId)
                ));

        if (product.isActive() == newStatus) {
            throw new InvalidRequestException(
                    String.format("Product status is already %s",
                    newStatus ? "Active" : "Inactive")
            );
        }

        product.setStatus(newStatus);
        Product updatedProduct = productRepository.save(product);

        if (newStatus) {
            productEventProducer.sendProductPublishedEvent(updatedProduct);
        } else {
            productEventProducer.sendProductUnpublishedEvent(updatedProduct.getProductId());
        }

        return productMapper.toProductResponse(updatedProduct);
    }

    public ProductResponse getProductById(Integer productId) {
        return productRepository.findById(productId)
                .map(productMapper::toProductResponse)
                .orElseThrow(() -> new ProductNotFoundException(
                        String.format("Product not found for customer ID: %s", productId)
                ));
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(productMapper::toProductResponse)
                .collect(Collectors.toList());
    }

    public Integer getCategoryIdOfProduct(Integer productId) {
        return productRepository.findById(productId)
                .map(product -> product.getCategory().getCategoryId())
                .orElseThrow(() -> new ProductNotFoundException(
                        String.format("Product not found for customer ID: %s", productId)
                ));
    }
}
