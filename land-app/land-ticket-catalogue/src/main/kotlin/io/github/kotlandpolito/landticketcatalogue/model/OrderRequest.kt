package io.github.kotlandpolito.landticketcatalogue.model

data class OrderRequest (
    val numberOfTickets: Int,
    val ticketId: String,
    val creditCardNumber: String,
    val creditCardExpirationDate: String,
    val creditCardCVV: String,
    val cardHolder: String
)