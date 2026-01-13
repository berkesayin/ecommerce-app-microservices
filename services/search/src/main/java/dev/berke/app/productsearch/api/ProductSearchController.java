package dev.berke.app.productsearch.api;

import dev.berke.app.productsearch.api.dto.AutocompleteSuggestionResponse;
import dev.berke.app.productsearch.api.dto.ProductSearchRequest;
import dev.berke.app.productsearch.api.dto.ProductSearchResponse;
import dev.berke.app.productsearch.application.ProductSearchService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/search/products")
@Validated
public class ProductSearchController {

    private final ProductSearchService productSearchService;

    @PostMapping
    public ResponseEntity<ProductSearchResponse> searchProducts(
            @RequestBody @Valid ProductSearchRequest request
    ) {
        return ResponseEntity.ok(productSearchService.searchProducts(request));
    }

    @GetMapping("/autocomplete")
    public ResponseEntity<AutocompleteSuggestionResponse> autocomplete(
            @NotBlank(message = "Query cannot be blank")
            @Size(min = 2, message = "Query must be at least 2 characters")
            @RequestParam("query")
            String query
    ) {
        return ResponseEntity.ok(productSearchService.getAutocompleteSuggestions(query));
    }
}