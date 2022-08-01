package io.github.kotlandpolito.landticketcatalogue.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "orders")
data class Order (
    @Id val id: String? = null,
    val userId: Long,
    val numberOfTickets: Int,
    val ticketId: AvailableTicket,
    var status: OrderStatus = OrderStatus.PENDING
){
    // added default constructor for debugging the /shop endpoint
    constructor() : this(null, 0, 0, AvailableTicket())
}