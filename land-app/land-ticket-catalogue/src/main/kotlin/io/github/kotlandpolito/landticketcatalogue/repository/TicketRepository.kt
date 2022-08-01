package io.github.kotlandpolito.landticketcatalogue.repository

import io.github.kotlandpolito.landticketcatalogue.model.AvailableTicket
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Mono

interface TicketRepository : ReactiveMongoRepository<AvailableTicket, String>{

    fun save(ticket: AvailableTicket): Mono<AvailableTicket>

}