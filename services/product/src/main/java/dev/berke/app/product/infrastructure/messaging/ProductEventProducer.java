package dev.berke.app.product.infrastructure.messaging;

import dev.berke.app.product.application.mapper.ProductMapper;
import dev.berke.app.product.domain.event.ProductPublishedEvent;
import dev.berke.app.product.domain.event.ProductUnpublishedEvent;
import dev.berke.app.product.domain.model.Product;
import dev.berke.app.shared.exception.EventPublishingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ProductEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String productEventsTopic;
    private final ProductMapper productMapper;

    public ProductEventProducer(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${app.kafka.topics.product-events}") String productEventsTopic,
            ProductMapper productMapper
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.productEventsTopic = productEventsTopic;
        this.productMapper = productMapper;
    }

    public void sendProductPublishedEvent(Product product) {
        log.info("Sending ProductPublishedEvent for product ID: {}", product.getProductId());

        ProductPublishedEvent event = productMapper.toProductPublishedEvent(product);
        sendEvent(String.valueOf(event.productId()), event);
    }

    public void sendProductUnpublishedEvent(Integer productId) {
        log.info("Sending ProductUnpublishedEvent for product ID: {}", productId);

        ProductUnpublishedEvent event = new ProductUnpublishedEvent(productId);
        sendEvent(String.valueOf(productId), event);
    }

    // if kafka is down, db transaction rolls back
    private void sendEvent(String key, Object event) {
        try {
            kafkaTemplate.send(productEventsTopic, key, event).join();

            log.info("Event sent successfully to topic '{}' with key '{}'",
                    productEventsTopic, key);
        } catch (Exception e) {
            log.error("Failed to send event to Kafka. Key: {}", key, e);
            throw new EventPublishingException("Failed to publish event to Kafka", e);
        }
    }
}