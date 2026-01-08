# Payment Service

`Payment` service handles `payment` operations. It is connected to `PostgreSQL` database. For that, `iyzico's` Java API client, which is `iyzipay-java` is used. 

- `iyzipay-java` Repository: https://github.com/iyzico/iyzipay-java
- Its dependency for `maven`:

```xml
<dependency>
  <groupId>com.iyzipay</groupId>
  <artifactId>iyzipay-java</artifactId>
  <version>2.0.140</version>
</dependency>
```

### Build and Run Payment Service

Make sure `docker containers`, `config-server` and `discovery` services are running.

Build the `payment` service. For that locate to `payment` at terminal: `cd services/payment`

```sh 
../../mvnw clean install
```

Run the service.

```sh 
../../mvnw spring-boot:run
```

### Payment Service Endpoints

`BASE_URL` = `/api/v1/payments`

| Method Type | Endpoint URL | Authorization | Function Name |
| :--- | :--- | :--- | :--- |
| POST | `Base URL/me/credit-cards` | `hasRole('USER')` | `createCreditCard` |
| GET  | `Base URL/me/credit-cards` | `hasRole('USER')` | `getCreditCards` |
| POST | `Base URL/iyzi-payment` | `hasRole('USER')` | `createPayment` |
| GET | `Base URL/{paymentId}` | `hasRole('BACKOFFICE')` | `getPaymentDetails` |
