package io.github.kotlandpolito.landticketcatalogue.controller

import io.github.kotlandpolito.landticketcatalogue.model.OrderRequest
import io.github.kotlandpolito.landticketcatalogue.service.OrderService
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyExtractors
import org.springframework.web.reactive.function.server.*

@Component
class OrderHandler(private val orderService: OrderService) {

    //POST /shop/{ticket-id}
    suspend fun createOrder(request: ServerRequest): ServerResponse {
        val ticketId = request.pathVariable("ticket-id")
        val orderRequest = request.body(BodyExtractors.toMono(OrderRequest::class.java)).awaitSingle()
        val userId: Long = request.principal().awaitSingle().name.toLong()

        val createdOrderId = request.headers().firstHeader("Authorization")
            ?.let { orderService.createOrder(ticketId, orderRequest, userId, it) }

        if (createdOrderId != null){
            return ServerResponse.ok().json().bodyValueAndAwait(createdOrderId)
        }
        return ServerResponse.badRequest().buildAndAwait()
    }


    //GET /orders
    suspend fun getAllOrdersOfUser(request: ServerRequest): ServerResponse {
        val userId: Long = request.principal().awaitSingle().name.toLong()
        return ServerResponse.ok().json().bodyAndAwait(orderService.getAllByUserId(userId))
    }


    //GET /orders/{order-id}
    suspend fun getOrderOfUserById(request: ServerRequest): ServerResponse {
        val userId: Long = request.principal().awaitSingle().name.toLong()
        val orderId = request.pathVariable("order-id")
        val order = orderService.getByUserIdAndId(userId, orderId)
        if (order != null){
            return ServerResponse.ok().json().bodyValueAndAwait(order)
        }
        return ServerResponse.notFound().buildAndAwait()
    }


    //GET /admin/orders
    suspend fun getAllOrdersAsAdmin(request: ServerRequest): ServerResponse {
        return ServerResponse.ok().json().bodyAndAwait(orderService.getAll())
    }


    //GET /admin/orders/{user-id}
    suspend fun getAllOrdersOfUserAsAdmin(request: ServerRequest): ServerResponse {
        val userId: Long = request.pathVariable("user-id").toLong()
        return ServerResponse.ok().json().bodyAndAwait(orderService.getAllByUserId(userId))
    }

}