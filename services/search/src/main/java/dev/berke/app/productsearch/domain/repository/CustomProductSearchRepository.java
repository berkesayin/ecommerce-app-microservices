package dev.berke.app.productsearch.domain.repository;

import dev.berke.app.productsearch.api.dto.ProductSearchRequest;
import dev.berke.app.productsearch.domain.document.ProductDocument;
import org.springframework.data.elasticsearch.core.SearchHits;

public interface CustomProductSearchRepository {

    SearchHits<ProductDocument> search(ProductSearchRequest productSearchRequest);
}
