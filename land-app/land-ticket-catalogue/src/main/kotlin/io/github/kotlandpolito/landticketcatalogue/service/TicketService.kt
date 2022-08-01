package io.github.kotlandpolito.landticketcatalogue.service

import io.github.kotlandpolito.landticketcatalogue.model.AvailableTicket
import io.github.kotlandpolito.landticketcatalogue.repository.TicketRepository
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Service

@Service
class TicketService(private val ticketRepository: TicketRepository) {
    suspend fun getAll() = ticketRepository.findAll().asFlow()

    suspend fun createTicket(ticket: AvailableTicket) = ticketRepository.save(ticket).awaitSingleOrNull()

}