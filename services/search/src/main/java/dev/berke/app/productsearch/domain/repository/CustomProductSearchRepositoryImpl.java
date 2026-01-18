package dev.berke.app.productsearch.domain.repository;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.json.JsonData;
import dev.berke.app.productsearch.api.dto.ProductSearchRequest;
import dev.berke.app.productsearch.domain.document.ProductDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CustomProductSearchRepositoryImpl implements CustomProductSearchRepository {

    private static final String AGG_CATEGORY = "category_agg";
    private static final String AGG_MANUFACTURER = "manufacturer_agg";
    private static final String FIELD_PRODUCT_NAME = "product_name";
    private static final String FIELD_CATEGORY_NAME = "category.name";
    private static final String FIELD_CATEGORY_KEYWORD = "category.name.keyword";
    private static final String FIELD_MANUFACTURER = "manufacturer";
    private static final String FIELD_MANUFACTURER_KEYWORD = "manufacturer.keyword";
    private static final String FIELD_MIN_PRICE = "min_price";
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_CREATED_ON = "created_on";
    private final ElasticsearchOperations elasticsearchOperations;

    // business logic to search products
    // full-text search
    // 1. build bool query (where)
    // 2. initialize native query builder
    // 3. add aggregations
    // 4. apply sorting

    @Override
    public SearchHits<ProductDocument> search(ProductSearchRequest request) {
        BoolQuery boolQuery = buildBoolQuery(request);

        NativeQueryBuilder nativeQueryBuilder =
                NativeQuery.builder()
                        .withQuery(Query.of(q -> q.bool(boolQuery)))
                        .withPageable(PageRequest.of(
                                request.page(),
                                request.size())
                        );

        addAggregations(nativeQueryBuilder);

        applySorting(nativeQueryBuilder, request.sortBy());

        return elasticsearchOperations.search(
                nativeQueryBuilder.build(),
                ProductDocument.class
        );
    }

    private BoolQuery buildBoolQuery(ProductSearchRequest request) {
        BoolQuery.Builder builder = new BoolQuery.Builder();

        addFullTextSearch(builder, request.query());
        addCategoryFilter(builder, request.categories());
        addManufacturerFilter(builder, request.manufacturers());
        addPriceFilter(builder, request.priceRange());
        addActiveStatusFilter(builder);

        return builder.build();
    }

    private void addFullTextSearch(BoolQuery.Builder builder, String query) {
        if (StringUtils.hasText(query)) {
            builder.must(q -> q.multiMatch(mm -> mm
                    .query(query)
                    .fields(
                            FIELD_PRODUCT_NAME,
                            FIELD_CATEGORY_NAME,
                            FIELD_MANUFACTURER
                    )
                    .fuzziness("AUTO")
            ));
        } else {
            builder.must(q -> q.matchAll(m -> m));
        }
    }

    private void addTermsFilter(
            BoolQuery.Builder builder,
            String fieldName,
            List<String> values
    ) {
        if (!CollectionUtils.isEmpty(values)) {
            builder.filter(f -> f.terms(t -> t
                    .field(fieldName)
                    .terms(v -> v.value(values
                                    .stream()
                                    .map(FieldValue::of)
                                    .toList()))
                    )
            );
        }
    }

    private void addCategoryFilter(
            BoolQuery.Builder builder,
            List<String> categories
    ) {
        addTermsFilter(
                builder,
                FIELD_CATEGORY_KEYWORD,
                categories
        );
    }

    private void addManufacturerFilter(
            BoolQuery.Builder builder,
            List<String> manufacturers
    ) {
        addTermsFilter(
                builder,
                FIELD_MANUFACTURER_KEYWORD,
                manufacturers
        );
    }

    private void addPriceFilter(
            BoolQuery.Builder builder,
            ProductSearchRequest.PriceRange priceRange
    ) {
        if (priceRange == null) return;

        builder.filter(f -> f.range(r -> {
            r.field(FIELD_MIN_PRICE);
            if (priceRange.min() != null) {
                r.gte(JsonData.of(priceRange.min().doubleValue()));
            }
            if (priceRange.max() != null) {
                r.lte(JsonData.of(priceRange.max().doubleValue()));
            }
            return r;
        }));
    }

    private void addActiveStatusFilter(BoolQuery.Builder builder) {
        builder.filter(f -> f.term(t -> t
                .field(FIELD_STATUS)
                .value(FieldValue.of(true))
        ));
    }

    private void addAggregations(NativeQueryBuilder builder) {
        builder.withAggregation(AGG_CATEGORY, Aggregation.of(a -> a
                .terms(t -> t
                        .field(FIELD_CATEGORY_KEYWORD)
                        .size(50))
        ));

        builder.withAggregation(AGG_MANUFACTURER, Aggregation.of(a -> a
                .terms(t -> t
                        .field(FIELD_MANUFACTURER_KEYWORD)
                        .size(50))
        ));
    }

    private void applySorting(
            NativeQueryBuilder builder,
            ProductSearchRequest.SortCriteria sortCriteria
    ) {
        if (sortCriteria == null) return;

        switch (sortCriteria) {
            case PRICE_ASC -> builder.withSort(s -> s
                    .field(f -> f
                            .field(FIELD_MIN_PRICE)
                            .order(SortOrder.Asc)
                    )
            );
            case PRICE_DESC -> builder.withSort(s -> s
                    .field(f -> f
                            .field(FIELD_MIN_PRICE)
                            .order(SortOrder.Desc)
                    )
            );
            case NEWEST -> builder.withSort(s -> s
                    .field(f -> f
                            .field(FIELD_CREATED_ON)
                            .order(SortOrder.Desc)
                    )
            );
            case RELEVANCE -> {
                // elasticsearch default
            }
        }
    }
}