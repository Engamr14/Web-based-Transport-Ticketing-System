package io.github.kotlandpolito.landticketcatalogue.repository

import io.github.kotlandpolito.landticketcatalogue.model.AvailableTicket
import io.github.kotlandpolito.landticketcatalogue.model.Order
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface OrderRepository : ReactiveMongoRepository<Order, String> {
    fun findAllByTicketId(ticketId: AvailableTicket): Flux<Order>
    fun findAllByUserId(userId: Long): Flux<Order>
    fun findByUserIdAndId(userId: Long, id: String): Mono<Order>
}