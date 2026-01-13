package dev.berke.app.productsearch.application;

import dev.berke.app.consumer.event.ProductPublishedEvent;
import dev.berke.app.productsearch.api.dto.SearchAggregation;
import dev.berke.app.productsearch.domain.document.ProductDocument;
import dev.berke.app.productsearch.api.dto.AutocompleteSuggestionResponse;
import dev.berke.app.productsearch.api.dto.ProductSearchRequest;
import dev.berke.app.productsearch.api.dto.ProductSearchResponse;
import dev.berke.app.productsearch.api.dto.ProductSearchResult;
import dev.berke.app.productsearch.domain.repository.ProductSearchRepository;
import dev.berke.app.shared.exception.InvalidSearchRequestException;
import dev.berke.app.shared.exception.SearchOperationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.core.AggregationsContainer;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.support.PageableExecutionUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsAggregate;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductSearchService {

    private final ProductSearchRepository productSearchRepository;

    public void indexProduct(ProductPublishedEvent event) {
        log.info("INDEX event received for product ID: {}", event.productId());

        try {
            ProductDocument document = mapToProductDocument(event);
            productSearchRepository.save(document);

            log.info("Successfully indexed product with ID: {}", document.getProductId());
        } catch (Exception e) {
            log.error("Failed to index product ID: {}", event.productId(), e);
        }
    }

    public void deleteProduct(Integer productId) {
        log.info("DELETE event received for product ID: {}", productId);
        productSearchRepository.deleteById(productId);
    }

    public ProductSearchResponse searchProducts(ProductSearchRequest request) {
        log.info("Searching for products. Query: [{}], Page: [{}]",
                request.query(), request.page());

        try {
            SearchHits<ProductDocument> searchHits = productSearchRepository.search(request);

            List<SearchAggregation> aggregations = extractAggregations(searchHits);
            Page<ProductSearchResult> page = toProductSearchResultPage(request, searchHits);

            return ProductSearchResponse.from(page, aggregations);
        } catch (Exception e) {
            throw new SearchOperationException("Failed to execute search query", e);
        }
    }

    public AutocompleteSuggestionResponse getAutocompleteSuggestions(String query) {
        if (query == null || query.trim().length() < 2) {
            throw new InvalidSearchRequestException(
                    "Autocomplete query must have at least 2 characters.");
        }

        List<ProductDocument> results =
                productSearchRepository.findByProductNameAutocomplete(query);

        List<String> suggestions = results.stream()
                .map(ProductDocument::getProductName)
                .distinct()
                .limit(10)
                .toList();

        return new AutocompleteSuggestionResponse(suggestions);
    }

    private ProductDocument mapToProductDocument(ProductPublishedEvent productPublishedEvent) {
        return ProductDocument.builder()
                .productId(productPublishedEvent.productId())
                .productName(productPublishedEvent.productName())
                .category(ProductDocument.CategoryDocument.builder()
                        .id(productPublishedEvent.categoryId().toString())
                        .name(productPublishedEvent.categoryName())
                        .build())
                .basePrice(productPublishedEvent.basePrice())
                .minPrice(productPublishedEvent.minPrice())
                .manufacturer(productPublishedEvent.manufacturer())
                .sku(productPublishedEvent.sku())
                .status(productPublishedEvent.status())
                .createdOn(productPublishedEvent.createdOn())
                .build();
    }

    private Page<ProductSearchResult> toProductSearchResultPage(
            ProductSearchRequest productSearchRequest,
            SearchHits<ProductDocument> searchHits
    ) {
        if (searchHits.getTotalHits() <= 0) {
            return Page.empty(PageRequest.of(
                    productSearchRequest.page(),
                    productSearchRequest.size())
            );
        }

        List<ProductSearchResult> results =
                searchHits
                        .getSearchHits()
                        .stream()
                        .map(hit -> ProductSearchResult
                                .from(hit.getContent())
                        )
                        .toList();

        return PageableExecutionUtils.getPage(
                results,
                PageRequest.of(productSearchRequest.page(), productSearchRequest.size()),
                searchHits::getTotalHits
        );
    }

    private List<SearchAggregation> extractAggregations(SearchHits<ProductDocument> searchHits) {
        AggregationsContainer<?> aggregationsContainer = searchHits.getAggregations();

        if (!(aggregationsContainer instanceof
                ElasticsearchAggregations elasticsearchAggregations)
        ) {
            return Collections.emptyList();
        }

        List<SearchAggregation> resultAggregations = new ArrayList<>();

        for (ElasticsearchAggregation aggregation : elasticsearchAggregations.aggregations()) {
            Aggregate aggregate = aggregation.aggregation().getAggregate();

            if (aggregate.isSterms()) {
                StringTermsAggregate termsAgg = aggregate.sterms();

                List<SearchAggregation.Bucket> buckets =
                        termsAgg.buckets()
                                .array()
                                .stream()
                                .map(bucket -> new SearchAggregation.Bucket(
                                        bucket.key().stringValue(),
                                        bucket.docCount()
                                ))
                                .toList();

                if (!buckets.isEmpty()) {
                    resultAggregations.add(
                            new SearchAggregation(
                                    aggregation.aggregation().getName(),
                                    buckets
                            ));
                }
            }
        }
        return resultAggregations;
    }
}
