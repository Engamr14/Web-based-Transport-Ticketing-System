package io.github.kotlandpolito.landticketcatalogue.controller

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.server.coRouter

@Configuration
class RouterConfiguration {

    @Bean
    fun mainRouter(ticketHandler: TicketHandler, orderHandler: OrderHandler) = coRouter {
        accept(APPLICATION_JSON).nest {
            POST("/shop/{ticket-id}", orderHandler::createOrder)
            GET("/tickets", ticketHandler::getAllTickets)
            GET("/orders", orderHandler::getAllOrdersOfUser)
            GET("/orders/{order-id})", orderHandler::getOrderOfUserById)
            POST("/admin/tickets", ticketHandler::createTicket)
            GET("/admin/orders", orderHandler::getAllOrdersAsAdmin)
            GET("/admin/orders/{user-id})", orderHandler::getAllOrdersOfUserAsAdmin)
        }
    }

}