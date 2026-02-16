package dev.berke.app.basket.api;

import dev.berke.app.basket.api.dto.BasketAddItemRequest;
import dev.berke.app.basket.api.dto.BasketResponse;
import dev.berke.app.basket.application.BasketService;
import dev.berke.app.basket.api.dto.BasketTotalPriceResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/baskets")
@RequiredArgsConstructor
public class BasketController {

    private final BasketService basketService;

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BasketResponse> getBasket(
            @AuthenticationPrincipal String customerIdPrincipal
    ){
        return ResponseEntity.ok(basketService.getBasket(customerIdPrincipal));
    }

    @PostMapping("/items")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BasketResponse> addItemToBasket(
            @AuthenticationPrincipal String customerIdPrincipal,
            @Valid @RequestBody BasketAddItemRequest basketAddItemRequest
    ) {
        return ResponseEntity.ok(
                basketService.addItemToBasket(customerIdPrincipal, basketAddItemRequest));
    }

    @GetMapping("/total-price")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BasketTotalPriceResponse> calculateTotalBasketPrice(
            @AuthenticationPrincipal String customerIdPrincipal
    ) {
        return ResponseEntity.ok(basketService.calculateTotalBasketPrice(customerIdPrincipal));
    }

    @PutMapping("/items/{productId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BasketResponse> updateItemQuantity(
            @AuthenticationPrincipal String customerIdPrincipal,
            @PathVariable Integer productId,
            @RequestParam @Min(value = 1, message = "Quantity must be at least 1") Integer quantity
    ) {
        return ResponseEntity.ok(
                basketService.updateItemQuantity(customerIdPrincipal, productId, quantity));
    }

    @DeleteMapping("/items/{productId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BasketResponse> removeBasketItem(
            @AuthenticationPrincipal String customerIdPrincipal,
            @PathVariable Integer productId
    ) {
        return ResponseEntity.ok(
                basketService.removeBasketItem(customerIdPrincipal, productId));
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> clearBasket(
            @AuthenticationPrincipal String customerIdPrincipal
    ) {
        basketService.clearBasket(customerIdPrincipal);
        return ResponseEntity.noContent().build();
    }
}
