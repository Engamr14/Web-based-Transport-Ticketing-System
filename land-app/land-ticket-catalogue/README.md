# land-ticket-catalogue

This repository contains the *TicketCatalogueService* microservice.

This microservice is in charge of:

- managing the tickets' sales process
- providing the list of ordinal and season tickets

## REST API

The microservice offers its functionalities by means of a REST API.

Specifically, it exposes the following endpoints:

---

- GET `/tickets`

  - Response status: `200 OK` or `500 Internal Server Error`
  - Response body: *a JSON list of all available tickets*

    ```json
    {
        [
            {
                price,
                ticketId,
                type
            },
            ...
        ]
    }
    ```

---

- POST `/shop/{ticket-id}`

  - Request parameters: *ticket-id*
  - Request headers: *Authentication*
  - Request body: *a JSON object containing the purchase information*

    ```json
    {
        numberOfTickets,
        ticketId,
        creditCardNumber,
        creditCardExpirationDate,
        creditCardCVV,
        cardHolder
    }
    ```

  - Response status: `201 Created` or `401 Unauthorized` or `400 Bad Request` or `500 Internal Server Error`
  - Response body: *a JSON object containing the order id*

    ```json
    {
        orderId
    }
    ```

---

- GET `/orders`

  - Request headers: *Authentication*
  - Response status: `200 OK` or `401 Unauthorized` or `500 Internal Server Error`
  - Response body: *a JSON list of all the orders of the requesting user*

    ```json
    {
        [
            {
                orderId,
                userId,
                numberOfTickets,
                ticketId,
                status
            },
            ...
        ]
    }
    ```

---

- GET `/orders/{order-id}`

  - Request parameters: *order-id*
  - Request headers: *Authentication*
  - Response status: `200 OK` or `401 Unauthorized` or `404 Not Found` or `500 Internal Server Error`
  - Response body: *a JSON object of the order identified by order-id*

    ```json
    {
        orderId,
        userId,
        numberOfTickets,
        ticketId,
        status
    }
    ```

---

- POST `/admin/tickets`

  - Request headers: *Authentication* (as Admin)
  - Request body: *a JSON object containing the new kind of ticket*

    ```json
    {
        price,
        type
    }
    ```

  - Response status: `201 Created` or `401 Unauthorized` or `400 Bad Request` or `500 Internal Server Error`

---

- GET `/admin/orders`

  - Request headers: *Authentication* (as Admin)
  - Response status: `200 OK` or `401 Unauthorized` or `500 Internal Server Error`
  - Response body: *a JSON list of all the orders made by all users*

    ```json
    {
        [
            {
                orderId,
                userId,
                numberOfTickets,
                ticketId,
                status
            },
            ...
        ]
    }
    ```

---

- GET `/admin/orders/{user-id}`

  - Request parameters: *user-id*
  - Request headers: *Authentication* (as Admin)
  - Response status: `200 OK` or `401 Unauthorized` or `404 Not Found` or `500 Internal Server Error`
  - Response body: *a JSON list of all the orders made by a specific user*

    ```json
    {
        [
            {
                orderId,
                userId,
                numberOfTickets,
                ticketId,
                status
            },
            ...
        ]
    }
    ```

## Setting up the MongoDB docker instance

The very first time, to create the container, run:

```bash
docker run --name mongo-db \
      -d -p 27017:27017 \
      -v $(pwd)/database:/data/db mongo
```

Then, you can start and stop the container running:

```bash
docker start mongo-db
# or
docker stop mongo-db
```
