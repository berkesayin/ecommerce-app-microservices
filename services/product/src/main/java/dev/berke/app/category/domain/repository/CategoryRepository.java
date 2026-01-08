package dev.berke.app.category.domain.repository;

import dev.berke.app.category.domain.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
}
