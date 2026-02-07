# Customer Service

`Customer` service handles customers operations and data. It is connected to the `MongoDB - customer` database. 

### Build and Run Customer Service

Make sure `docker containers`, `config-server` and `discovery` services are running.

Build the `customer` service. For that locate to `customer` at terminal: `cd services/customer`.

```sh 
../../mvnw clean install
```

Run the service.

```sh 
../../mvnw spring-boot:run
```

### Customer Service Endpoints

`BASE_URL` = `/api/v1/customers`

| Method Type | Endpoint URL | Authorization | Function Name |
| :--- | :--- | :--- | :--- |
| POST | `Base URL` | `Public Endpoint` | `createCustomer` |
| PATCH  | `Base URL/me/identity` | `hasRole('USER')` | `updateIdentity` |
| PATCH  | `Base URL/me/contact` | `hasRole('USER')` | `updateContact` |
| GET | `Base URL/me` | `hasRole('USER')` | `getProfile` |
| DELETE | `Base URL/me` | `hasRole('USER')` | `deleteProfile` |
| POST | `Base URL/me/billing-addresses` | `hasRole('USER')` | `addBillingAddress` |
| POST | `Base URL/me/shipping-addresses` | `hasRole('USER')` | `addShippingAddress` |
| PUT | `Base URL/me/billing-addresses/{billingAddressId}/active` | `hasRole('USER')` | `setActiveBillingAddress` |
| PUT | `Base URL/me/shipping-addresses/{shippingAddressId}/active` | `hasRole('USER')` | `setActiveShippingAddress` |
| DELETE | `Base URL/me/billing-addresses/{billingAddressId}` | `hasRole('USER')` | `deleteBillingAddress` |
| DELETE | `Base URL/me/shipping-addresses/{shippingAddressId}` | `hasRole('USER')` | `deleteShippingAddress` |
| GET | `Base URL/me/billing-addresses` | `hasRole('USER')` | `getBillingAddresses` |
| GET | `Base URL/me/shipping-addresses` | `hasRole('USER')` | `getShippingAddresses` |
| GET | `Base URL/me/billing-addresses/active` | `hasRole('USER')` | `getActiveBillingAddress` |
| GET | `Base URL/me/shipping-addresses/active` | `hasRole('USER')` | `getActiveShippingAddress` |
| GET | `Base URL` | `hasRole('BACKOFFICE')` | `getAllCustomers` |
| GET | `Base URL/exists/{customerId}` | `hasRole('BACKOFFICE')` | `checkCustomerById` |
| GET | `Base URL/{customerId}` | `hasRole('BACKOFFICE')` | `getCustomerById` |
| GET | `Base URL/{customerId}/billing-addresses` | `hasRole('BACKOFFICE')` | `getBillingAddressesByCustomerId` |
| GET | `Base URL/{customerId}/shipping-addresses` | `hasRole('BACKOFFICE')` | `getShippingAddressesByCustomerId` |
