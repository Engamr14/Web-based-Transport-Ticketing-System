# land-traveler
TravelerService microservice

# possible schema

TravelerService is composed by these endpoints:

-GET traveler/profile (Get UserDetails)
-POST traveler/profile (Insert new UserDetails)
-PUT traveler/profile (Update the UserDetails)

-GET traveler/tickets (Get the list of a traveler purchased)
-POST traveler/tickets (Insert the new buyed ticket)

-POST traveler/tickets/validate (Get if a ticket, passed by body belongs to the traveler and is in a valid format/has a valid signature)

-GET traveler/tickets/{ticketId}/download (Download the ticket as a QrCode that encapsulates a jwt)