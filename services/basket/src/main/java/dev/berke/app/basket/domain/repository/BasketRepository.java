package dev.berke.app.basket.domain.repository;

import dev.berke.app.basket.domain.model.Basket;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BasketRepository extends CrudRepository<Basket, String> {
}