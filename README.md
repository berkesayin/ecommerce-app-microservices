# eCommerce Application With Elastic Data And iyzico Payment

The `eCommerce app` is designed using `Microservices` architecture and `API Gateway` pattern where each service is responsible for a specific business function. 

`iyipay-java` API client developed by `iyzico` is integrated and used for the project at the `payment` service.

`Elastic's eCommerce Orders Data` is added from `Kibana Integrations` and used for `product`, `category`, `search` and `order` functionalities.

`Reference`: https://github.com/iyzico/iyzipay-java

![img](./docs/img/39.iyzico-sandbox.png)

`Architectural Diagram`

![img](./docs/img/40.app-architecture.png)

`NOTE:` Check docs or diagrams for `internal service calls` and `Kafka events`. 

`Elasticsearch` &rarr; `product` index as example

![img](./docs/img/41.kibana-product-index.png)

### Technologies  

Software architectures and principles:
- `Microservices`
- `API gateway`
- `Distributed systems`
- `OOP`
- `SOLID`
- `Domain driven design`
- `Rich domain model`
- `Event driven architecture`
- `CQRS (product, search)`
- `Monitoring, observability`
- `Exception handling`

Technologies:
- `Java 21`
- `Spring Boot 3.3.1`
- `Spring Security`
- `iyzipay-java API Client`
- `Maven`
- `GitHub Actions`
- `Elasticsearch`
- `Kibana`
- `Elastic Agent` and `Fleet Server`
- `Elastic APM` and `OpenTelemetry`
- `Docker`
- `PostgreSQL`
- `MongoDB`
- `Redis`
- `Kafka`

### Event Driven Architecture

`NOTE:` Check docs or diagrams for `asynchronous messaging` with `Kafka events`. 

- `ProductPublished`
- `ProductUnpublished`
- `OrderCreated`
- `OrderReceived`
- `PaymentReceived`

### Documentation

Refer to the `docs` section for `setup` and `development` of the project. It includes these parts:

- [1. Set Up iyzipay Java Client for Payment service](https://github.com/berkesayin/ecommerce-app-microservices/blob/master/docs/1.1.IYZIPAY_API_KEY.md)
  - [1.1. Get API Key for iyzipay Java Client](https://github.com/berkesayin/ecommerce-app-microservices/blob/master/docs/1.1.IYZIPAY_API_KEY.md)
  - [1.2. Use iyzico Sandbox Environment for Order Details](https://github.com/berkesayin/ecommerce-app-microservices/blob/master/docs/1.2.IYZICO_SANDBOX.md)
  - [1.3. Payment Details](https://github.com/berkesayin/ecommerce-app-microservices/blob/master/docs/1.3.PAYMENT_DETAILS.md)
- [2. Set Up Environment Variables](https://github.com/berkesayin/ecommerce-app-microservices/blob/master/docs/2.ENVIRONMENT_VARIABLES.md)
- [3. Run Docker Containers](https://github.com/berkesayin/ecommerce-app-microservices/blob/master/docs/3.DOCKER_COMPOSE_SERVICES.md)
- [4. Run Elasticsearch and Kibana](https://github.com/berkesayin/ecommerce-app-microservices/blob/master/docs/4.1.ELASTICSEARCH_KIBANA_SETUP.md)
  - [4.1. Set up Elasticsearch and Kibana](https://github.com/berkesayin/ecommerce-app-microservices/blob/master/docs/3.1.ELASTICSEARCH_KIBANA_SETUP.md)
  - [4.2. Get Elastic's Sample eCommerce Data](https://github.com/berkesayin/ecommerce-app-microservices/blob/master/docs/4.2.ECOMMERCE_DATA_ELASTIC.md)
  - [4.3. Get Product Data for Elasticsearch](https://github.com/berkesayin/ecommerce-app-microservices/blob/master/docs/4.3.PRODUCT_DATA_ELASTICSEARCH.md)
  - [4.4. Get Category Data for Elasticsearch](https://github.com/berkesayin/ecommerce-app-microservices/blob/master/docs/4.4.CATEGORY_DATA_ELASTICSEARCH.md)
  - [4.5. Get Product Data for PostgreSQL](https://github.com/berkesayin/ecommerce-app-microservices/blob/master/docs/4.5.PRODUCT_DATA_POSTGRESQL.md)
  - [4.6. Get Customer Data](https://github.com/berkesayin/ecommerce-app-microservices/blob/master/docs/4.6.CUSTOMER_DATA.md)
  - [4.7. Create Order Index at Elasticsearch](https://github.com/berkesayin/ecommerce-app-microservices/blob/master/docs/4.7.ORDER_INDEX_ELASTICSEARCH.md)
  - [4.8. Go to Kibana Index Management](https://github.com/berkesayin/ecommerce-app-microservices/blob/master/docs/4.8.INDEX_MANAGEMENT_KIBANA.md)
- [5. Use Kibana Observability](https://github.com/berkesayin/ecommerce-app-microservices/blob/master/docs/5.1.FLEET_SERVER_ELASTIC_AGENT.md)
  - [5.1. Set Up Fleet Server and Elastic Agent](https://github.com/berkesayin/ecommerce-app-microservices/blob/master/docs/5.1.FLEET_SERVER_ELASTIC_AGENT.md)
  - [5.2. Add Kibana's Docker Integration](https://github.com/berkesayin/ecommerce-app-microservices/blob/master/docs/5.2.KIBANA_INTEGRATIONS_DOCKER.md)
  - [5.3. Add Kibana's Redis Integration](https://github.com/berkesayin/ecommerce-app-microservices/blob/master/docs/5.3.KIBANA_INTEGRATIONS_REDIS.md)
  - [5.4. Use Elastic APM (Application Performance Monitoring)](https://github.com/berkesayin/ecommerce-app-microservices/blob/master/docs/5.4.ELASTIC_APM.md)
  - [5.5. Use OpenTelemetry](https://github.com/berkesayin/ecommerce-app-microservices/blob/master/docs/5.5.OPEN_TELEMETRY.md)
- [6. Use Search Functions](https://github.com/berkesayin/ecommerce-app-microservices/blob/master/docs/6.SEARCH_FUNCTIONS.md)
- [7. Run Backend Services](https://github.com/berkesayin/ecommerce-app-microservices/blob/master/docs/7.BACKEND_SERVICES.md)
  - [auth](https://github.com/berkesayin/ecommerce-app-microservices/tree/master/services/auth)
  - [basket](https://github.com/berkesayin/ecommerce-app-microservices/tree/master/services/basket)
  - [config-server](https://github.com/berkesayin/ecommerce-app-microservices/tree/master/services/config-server)
  - [customer](https://github.com/berkesayin/ecommerce-app-microservices/tree/master/services/customer)
  - [discovery](https://github.com/berkesayin/ecommerce-app-microservices/tree/master/services/discovery)
  - [gateway](https://github.com/berkesayin/ecommerce-app-microservices/tree/master/services/gateway)
  - [notification](https://github.com/berkesayin/ecommerce-app-microservices/tree/master/services/notification)
  - [order](https://github.com/berkesayin/ecommerce-app-microservices/tree/master/services/order)
  - [payment](https://github.com/berkesayin/ecommerce-app-microservices/tree/master/services/payment)
  - [product](https://github.com/berkesayin/ecommerce-app-microservices/tree/master/services/product)
  - [search](https://github.com/berkesayin/ecommerce-app-microservices/tree/master/services/search)

### New Features 

- Running the project in `Kubernetes` environment:
  - `minikube`
  - `kubectl`
  - `helm`
  - `kustomize`

This part is under development and will be released with new version of the project.

### Contributing 

Contributions are welcome! If you have suggestions or want to improve the code, please check [Contributing](https://github.com/berkesayin/ecommerce-app-microservices/blob/master/CONTRIBUTING.md) section. 

### License 

Released under the [MIT License](https://github.com/berkesayin/ecommerce-app-microservices/blob/master/LICENSE).