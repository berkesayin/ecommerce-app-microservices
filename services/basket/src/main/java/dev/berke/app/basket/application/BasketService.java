package dev.berke.app.basket.application;

import dev.berke.app.basket.application.mapper.BasketMapper;
import dev.berke.app.basket.domain.model.Basket;
import dev.berke.app.basket.domain.repository.BasketRepository;
import dev.berke.app.basket.api.dto.BasketAddItemRequest;
import dev.berke.app.basket.api.dto.BasketResponse;
import dev.berke.app.basket.api.dto.BasketTotalPriceResponse;
import dev.berke.app.basket.infrastructure.client.product.ProductClient;
import dev.berke.app.shared.exception.BasketNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class BasketService {

    private final BasketRepository basketRepository;
    private final ProductClient productClient;
    private final BasketMapper basketMapper;

    public BasketResponse getBasket(String customerId) {
        Basket basket = getBasketOrThrow(customerId);

        return basketMapper.toBasketResponse(basket);
    }

    public BasketResponse addItemToBasket(String customerId, BasketAddItemRequest request) {
        Basket basket = basketRepository.findById(customerId)
                .orElse(new Basket(customerId));

        request.items().forEach(itemRequest -> {
            var productResponse = productClient.getProductById(itemRequest.productId());
            var basketItem = basketMapper.toBasketItem(productResponse, itemRequest.quantity());

            basket.addItem(basketItem);
        });

        Basket updatedBasket = basketRepository.save(basket);

        return basketMapper.toBasketResponse(updatedBasket);
    }

    public BasketTotalPriceResponse calculateTotalBasketPrice(String customerId) {
        Basket basket = getBasketOrThrow(customerId);
        BigDecimal total = basket.calculateTotalPrice();

        return new BasketTotalPriceResponse(customerId, total);
    }

    public BasketResponse updateItemQuantity(
            String customerId,
            Integer productId,
            Integer newQuantity
    ) {
        Basket basket = getBasketOrThrow(customerId);
        basket.changeQuantity(productId, newQuantity);
        Basket updatedBasket = basketRepository.save(basket);

        return basketMapper.toBasketResponse(updatedBasket);
    }

    public BasketResponse removeBasketItem(String customerId, Integer productId) {
        Basket basket = getBasketOrThrow(customerId);
        basket.removeProduct(productId);

        Basket updatedBasket = basketRepository.save(basket);

        return basketMapper.toBasketResponse(updatedBasket);
    }

    public void clearBasket(String customerId) {
        Basket basket = getBasketOrThrow(customerId);
        basket.clearBasket();

        basketRepository.save(basket);
    }

    private Basket getBasketOrThrow(String customerId) {
        return basketRepository.findById(customerId)
                .orElseThrow(() -> new BasketNotFoundException(
                        String.format("Basket not found for customer ID: %s", customerId)
                ));
    }
}