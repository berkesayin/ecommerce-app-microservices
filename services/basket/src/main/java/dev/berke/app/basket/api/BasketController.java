package dev.berke.app.basket.api;

import dev.berke.app.basket.api.dto.BasketAddItemRequest;
import dev.berke.app.basket.api.dto.BasketResponse;
import dev.berke.app.basket.application.BasketService;
import dev.berke.app.basket.api.dto.BasketTotalPriceResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
        return ResponseEntity.ok(basketService.addItemToBasket(customerIdPrincipal, basketAddItemRequest));
    }

    @GetMapping("/total-price")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BasketTotalPriceResponse> calculateTotalBasketPrice(
            @AuthenticationPrincipal String customerIdPrincipal
    ) {
        return ResponseEntity.ok(basketService.calculateTotalBasketPrice(customerIdPrincipal));
    }
}
