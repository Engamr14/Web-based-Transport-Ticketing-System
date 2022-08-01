package io.github.kotlandpolito.landticketcatalogue.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "tickets")
data class AvailableTicket (
    @Id
    val id: String? = null,
    val price: Float,
    val type: AvailableTicketType,
    val minAge: Int = -1,
    val maxAge: Int = -1,
    val zid: String
){
    // added default constructor for debugging the /shop endpoint
    constructor(): this(price=0.0f, type=AvailableTicketType.ORDINAL, zid="")
}
