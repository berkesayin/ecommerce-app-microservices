package dev.berke.app.product.api;

import dev.berke.app.product.api.dto.ProductCategoryChangeRequest;
import dev.berke.app.product.api.dto.ProductCreateRequest;
import dev.berke.app.product.api.dto.ProductResponse;
import dev.berke.app.product.api.dto.ProductUpdateRequest;
import dev.berke.app.product.application.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProductById(
            @PathVariable("productId") Integer productId
    ) {
        return ResponseEntity.ok(productService.getProductById(productId));
    }

    @PostMapping
    @PreAuthorize("hasRole('BACKOFFICE')")
    public ResponseEntity<ProductResponse> createProduct(
            @RequestBody @Valid ProductCreateRequest productCreateRequest
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(productService.createProduct(productCreateRequest));
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('BACKOFFICE')")
    public ResponseEntity<ProductResponse> updateProductDetails(
            @PathVariable Integer productId,
            @RequestBody @Valid ProductUpdateRequest productUpdateRequest
    ) {
        return ResponseEntity.ok(productService
                .updateProductDetails(productId, productUpdateRequest));
    }

    @GetMapping("/{productId}/category-id")
    @PreAuthorize("hasRole('BACKOFFICE')")
    public ResponseEntity<Integer> getCategoryIdOfProduct(
            @PathVariable("productId") Integer productId
    ) {
        return ResponseEntity.ok(productService.getCategoryIdOfProduct(productId));
    }

    @PutMapping("/{productId}/category")
    @PreAuthorize("hasRole('BACKOFFICE')")
    public ResponseEntity<ProductResponse> changeProductCategory(
            @PathVariable Integer productId,
            @RequestBody @Valid ProductCategoryChangeRequest productCategoryChangeRequest
    ) {
        return ResponseEntity.ok(
                productService.changeProductCategory(
                        productId,
                        productCategoryChangeRequest.newCategoryId()
                )
        );
    }

    @PatchMapping("/{productId}/publish")
    @PreAuthorize("hasRole('BACKOFFICE')")
    public ResponseEntity<Void> publishProduct(@PathVariable Integer productId) {
        productService.publishProduct(productId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{productId}/unpublish")
    @PreAuthorize("hasRole('BACKOFFICE')")
    public ResponseEntity<Void> unpublishProduct(@PathVariable Integer productId) {
        productService.unpublishProduct(productId);
        return ResponseEntity.noContent().build();
    }
}