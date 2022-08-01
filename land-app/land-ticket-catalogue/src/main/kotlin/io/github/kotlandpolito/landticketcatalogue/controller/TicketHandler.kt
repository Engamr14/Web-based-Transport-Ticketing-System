package io.github.kotlandpolito.landticketcatalogue.controller

import io.github.kotlandpolito.landticketcatalogue.model.AvailableTicket
import io.github.kotlandpolito.landticketcatalogue.service.TicketService
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.reactive.function.BodyExtractors
import org.springframework.web.reactive.function.server.*

@Component
class TicketHandler (private val ticketService: TicketService) {

    suspend fun getAllTickets(request: ServerRequest): ServerResponse {
        return ServerResponse.ok().json().bodyAndAwait(ticketService.getAll())
    }


    suspend fun createTicket(request: ServerRequest): ServerResponse {
        val ticket = request.body(BodyExtractors.toMono(AvailableTicket::class.java)).awaitSingle()
        val created = ticketService.createTicket(ticket)
        if (created != null){
            return ServerResponse.ok().buildAndAwait()
        }
        return ServerResponse.badRequest().buildAndAwait()
    }

}