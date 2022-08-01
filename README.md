# Public-Transport-Web-based-Ticketing-System
"Public Transport Web-based Ticketing Information System" Backend build on microservices architecture basis

## System Describtion
It is a web-based information system that supports a public transport company in managing the ticketing process and in granting automatic access to the vehicles.

The system will support two kinds of human users, travelers and administrators, as well as a set of embedded systems controlling the turnstiles at the entrance and exit gates.

Travelers will be able to register and create an account by providing a valid e-mail address they are in control of. Once logged in, travelers can manage their profile, buy tickets and travelcards, consult the list of their purchases and download single travel documents in the form of QRCodes encoding a JWS (JSON Web Signature).

QRCode readers located at the entrance of the transport stations will validate the JWS and drive/block the corresponding turnstiles. These devices will interact with the overall system authenticating themselves as embedded systems, in order to get the secret used to check the validity of the JWS and provide transit count information.

Administrators (i.e, employees of the transport company) will be enrolled via an administrative end-point by other administrators (provided they have the enrolling capability). At installation time, a single administrative username/password with enrolling capability will be created in order to bootstrap the process.
Administrators can manage ticket and travelcard types, by creating, updating and modifying their properties (validity period, price, usage conditions) as well as access reports about purchases and transits, both of single travelers and of the company as a whole for a selectable time period.

## System Definition in points
Three Main Functions:
- Manage users registration and authentication
- Manage ticketing process
- Granting automatic access to vehicles

Three Users:
- Traveler
- Administrator
- QR Code Reader (Embedded Systems)

## System Design
I have adopted a microservice architecture. Four main microservices implementing Business Logic:
- Login Service
- Traveler Service
- Ticket Catalogue Service
- Payment Service

Plus two cloud logistic services:
- Discovery Service
- API Gateway

## Cloud Services in Details

### Service Discovery
Service Discovery allows a microservice to discover and reach other microservices.

I have used the Eureka Service Discovery provided by Spring Cloud and Netflix, where all services automatically register themselves with Eureka server at startup.

### API Gateway
API Gateway uses routes to forward requests and responses to the destination services. It can apply filtering and modify requests and responses passing through it.

We used the API Gateway provided by Spring Cloud.

## Microservices in Details

### Authentication Service
This service is responsible for managing the registration and authentication of all users, including embedded systems.
It allows the following operations:
- Register new Travelers (by the traveler itself)
- Enroll new Admin or QR code Reader (by admin with enrolling capabilities)
- Login for all users (Travelers/ Admins/ QR code Readers)

Nb: in our system, Admins are also considered to be Travelers themselves

### Traveler Service
This service is responsible for managing the Traveler’s profile, tickets, and transits. It allows the following operations:
- Create Traveler profile (by a Traveler)
- Edit Traveler profile (by a Traveler)
- View Traveler profile (by a Traveler or Admin)
- View Traveler tickets and transits (by a Traveler or Admin)
- Add tickets to a Traveler (by an Admin)
- View all Travelers (by an Admin)
- Download Traveler’s Ticket in QRCode format (by a Traveler)
- Validate a Ticket (by an Admin or a QRCode Reader)

### Ticket Catalogue Service
This service is responsible for managing available Tickets, their types, their properties, and allow a Traveler to buy them. It allows the following operations:
- Create a new type of ticket (by an Admin)
- View all available tickets (by everyone)
- Create an order (by a Traveler)
- Retrieve the details of an order (by a Traveler or Admin)
- Get all orders of a user (by a Traveler or Admin)
- Get all orders of all users (by an Admin)

### Payment Service
This service is responsible for managing payments. It collects Payment Requests submitted from the Ticket Catalogue Service, and sends them to the Payment Provider. The response is then forwarded back to the Ticket Catalogue.
All communications take place asynchronously by means of Apache Kafka.

## System Overview


## Possible Usage Scenario
Overall, possible usages of the system include:
- Traveler registration
- Admin registration
- Available Tickets creation
- Tickets purchase
- Ticket download in QR Code format
- Ticket validation

## Examples
### Ticket purchase
1. Anyone can retrieve the list of available Tickets.
2. The Traveler authenticates.
3. The Traveler tries to place an Order.
4. The Ticket Catalogue eventually asks the Traveler Service for more information (like
5. If the Order is placed correctly, a Payment Request is sent to the Payment Service , and the OrderID is returned to the
6. The Payment Service forwards the request to the bank.
7. The Traveler can check the status of the Order at any moment.
8. The Payment Outcome is forwarded to the Ticket Catalogue.
9. If everything went smoothly, the Tickets are added to the Travelers’ tickets

### Ticket validation (by QR Code Reader)
1. The QR Code Reader authenticates at boot.
2. The Traveler approaches the gate and scans the previously downloaded QR Code.
3. The QR Code is decoded into the JWS of a Ticket.
4. The QR Code Reader asks the Traveler Service whether the JWS of the ticket exists and is still valid
5. If the Ticket is valid, the QR Code Reader signals the gates to open , else the gates remain closed