package dev.berke.app.basket.application.mapper;

import dev.berke.app.basket.api.dto.BasketResponse;
import dev.berke.app.basket.domain.model.Basket;
import dev.berke.app.basket.domain.model.BasketItem;
import dev.berke.app.basket.domain.model.ItemType;
import dev.berke.app.basket.infrastructure.client.product.ProductResponse;
import org.springframework.stereotype.Component;

@Component
public class BasketMapper {

    public BasketResponse toBasketResponse(Basket basket) {
        if (basket == null) return null;
        return new BasketResponse(basket.getCustomerId(), basket.getItems());
    }

    public BasketItem toBasketItem(ProductResponse productResponse, Integer quantity) {
        return new BasketItem(
                productResponse.productId(),
                productResponse.productName(),
                productResponse.basePrice(),
                productResponse.manufacturer(),
                productResponse.categoryId(),
                ItemType.PHYSICAL,
                quantity
        );
    }
}
