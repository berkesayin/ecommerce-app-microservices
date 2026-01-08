package dev.berke.app.basket.application;

import dev.berke.app.basket.domain.model.Basket;
import dev.berke.app.basket.domain.model.BasketItem;
import dev.berke.app.basket.domain.repository.BasketRepository;
import dev.berke.app.basket.domain.model.ItemType;
import dev.berke.app.basket.api.dto.BasketAddItemRequest;
import dev.berke.app.basket.api.dto.BasketResponse;
import dev.berke.app.basket.api.dto.BasketTotalPriceResponse;
import dev.berke.app.basket.infrastructure.client.product.ProductClient;
import dev.berke.app.basket.infrastructure.client.product.ProductResponse;
import dev.berke.app.shared.exception.BasketNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BasketService {

    private final BasketRepository basketRepository;
    private final ProductClient productClient;

    public BasketResponse getBasket(String customerId) {
        Basket basket = basketRepository.findById(customerId)
                .orElseThrow(() -> new BasketNotFoundException(
                        String.format("Basket not found for customer ID: %s", customerId)
                ));

        return new BasketResponse(
                basket.getCustomerId(),
                basket.getItems()
        );
    }

    public BasketResponse addItemToBasket(
            String customerId,
            BasketAddItemRequest basketAddItemRequest
    ) {
        Basket basket = basketRepository.findById(customerId)
                .orElse(new Basket(customerId, new ArrayList<>()));

        // transform new items
        List<BasketItem> newBasketItems = basketAddItemRequest.items().stream()
                .map(basketItemRequest -> {
                    ProductResponse productResponse = productClient
                            .getProductById(basketItemRequest.productId());
                    return new BasketItem(
                            productResponse.productId(),
                            productResponse.productName(),
                            productResponse.basePrice(),
                            productResponse.manufacturer(),
                            productResponse.categoryId(),
                            ItemType.PHYSICAL,
                            basketItemRequest.quantity()
                    );
                }).collect(Collectors.toList());

        // add/update items
        List<BasketItem> existingItems = basket.getItems() != null ? basket.getItems() : new ArrayList<>();
        for(BasketItem newBasketItem : newBasketItems){
            boolean itemExists = false;
            for(BasketItem existingItem : existingItems){
                if(existingItem.getProductId().equals(newBasketItem.getProductId())){
                    existingItem.setQuantity(existingItem.getQuantity() + newBasketItem.getQuantity());
                    itemExists = true;
                    break;
                }
            }
            if(!itemExists){
                existingItems.add(newBasketItem);
            }
        }

        basket.setItems(existingItems);
        Basket updatedBasket = basketRepository.save(basket);

        return new BasketResponse(updatedBasket.getCustomerId(), updatedBasket.getItems());
    }

    public BasketTotalPriceResponse calculateTotalBasketPrice(String customerId) {
        Basket basket = basketRepository.findById(customerId)
                .orElseThrow(() -> new BasketNotFoundException(
                        String.format("Basket not found for customer ID: %s", customerId)
                ));

        List<BasketItem> items = basket.getItems();

        if (items == null || items.isEmpty()) {
            return new BasketTotalPriceResponse(customerId, BigDecimal.ZERO);
        }

        BigDecimal totalPrice = items.stream()
                .map(item -> {
                    BigDecimal price = item.getBasePrice() != null ? item.getBasePrice() : BigDecimal.ZERO;

                    return price.multiply(BigDecimal.valueOf(item.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new BasketTotalPriceResponse(customerId, totalPrice);
    }
}