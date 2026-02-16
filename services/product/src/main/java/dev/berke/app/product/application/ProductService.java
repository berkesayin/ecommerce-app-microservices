package dev.berke.app.product.application;

import dev.berke.app.category.domain.model.Category;
import dev.berke.app.category.domain.repository.CategoryRepository;
import dev.berke.app.product.api.dto.ProductUpdateRequest;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final ProductEventProducer productEventProducer;

    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request) {
        if (productRepository.existsBySku(request.sku())) {
            throw new ProductAlreadyExistsException(request.sku());
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException(request.categoryId().toString()));

        Product product = productMapper.toProduct(request, category);

        Product savedProduct = productRepository.save(product);

        log.info("Product created with ID: {}", savedProduct.getProductId());
        return productMapper.toProductResponse(savedProduct);
    }

    @Transactional
    public ProductResponse updateProductDetails(
            Integer productId,
            ProductUpdateRequest productUpdateRequest
    ) {
        Product product = getProductOrThrow(productId);

        product.updateDetails(
                productUpdateRequest.productName(),
                productUpdateRequest.basePrice(),
                productUpdateRequest.minPrice()
        );

        Product savedProduct = productRepository.save(product);
        return productMapper.toProductResponse(savedProduct);
    }

    @Transactional
    public ProductResponse changeProductCategory(Integer productId, Integer newCategoryId) {
        Product product = getProductOrThrow(productId);

        Category newCategory = categoryRepository.findById(newCategoryId)
                .orElseThrow(() -> new CategoryNotFoundException(
                        String.format("Category not found with ID: %s", newCategoryId)
                ));

        product.changeCategory(newCategory);

        Product savedProduct = productRepository.save(product);
        return productMapper.toProductResponse(savedProduct);
    }

    @Transactional
    public void publishProduct(Integer productId) {
        Product product = getProductOrThrow(productId);
        product.publish();

        Product savedProduct = productRepository.save(product);

        productEventProducer.sendProductPublishedEvent(savedProduct);
    }

    @Transactional
    public void unpublishProduct(Integer productId) {
        Product product = getProductOrThrow(productId);
        product.unpublish();

        productRepository.save(product);

        productEventProducer.sendProductUnpublishedEvent(product.getProductId());
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Integer productId) {
        return productMapper.toProductResponse(getProductOrThrow(productId));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(productMapper::toProductResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Integer getCategoryIdOfProduct(Integer productId) {
        return getProductOrThrow(productId).getCategory().getCategoryId();
    }

    private Product getProductOrThrow(Integer productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(
                        String.format("Product not found with ID: %s", productId)
                ));
    }
}
