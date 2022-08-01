package io.github.kotlandpolito.landpayment.controllers

import io.github.kotlandpolito.landpayment.service.TransactionService
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyAndAwait
import org.springframework.web.reactive.function.server.json

@Component
class TransactionHandler(val transactionService: TransactionService) {

    suspend fun getAllTransactionsOfAllUsers(request: ServerRequest): ServerResponse {
        return ServerResponse.ok().json().bodyAndAwait(transactionService.findAll())
    }

    suspend fun getAllTransactionsOfSingleUser(request: ServerRequest): ServerResponse {
        val userId: Long = request.principal().awaitSingle().name.toLong()
        return ServerResponse.ok().json().bodyAndAwait(transactionService.findAllByUserId(userId))
    }

}