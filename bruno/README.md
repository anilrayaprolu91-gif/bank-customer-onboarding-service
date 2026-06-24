# Bruno Collection - Bank Customer Onboarding Service

## Import

Open Bruno and import the folder:

```text
D:\bank-customer-onboarding-service\bruno
```

If your Bruno build rejects the native collection format, import the Postman-compatible file instead:

```text
D:\bank-customer-onboarding-service\bruno\bank-customer-onboarding.postman_collection.json
```

In Bruno, use **Import Collection** and choose the Postman collection option for that file.

## Environment

Use the `Local` environment:

- `baseUrl = http://localhost:8080`

The collection auto-populates these variables during execution:

- `customerId`
- `customerNumber`
- `accountId`

## Recommended Run Order

1. `01 Onboard Customer`
2. `04 Update Customer Status`
3. `05 Get Onboarding Status`
4. `06 Create Account`
5. `07 Get Accounts For Customer`
6. `08 Get Account By Id`
7. `09 Close Account`

## Notes

- `01 Onboard Customer` generates unique onboarding data on each run.
- `01 Onboard Customer` stores `customerId` and `customerNumber` from the response.
- `06 Create Account` stores `accountId` from the response.
- The later requests reuse those variables automatically.

## Base URL

If the service is running locally:

```text
http://localhost:8080
```

If you run the app in Docker Compose, keep the same base URL as long as the container maps port `8080` to the host.

