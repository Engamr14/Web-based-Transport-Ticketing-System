package io.github.kotlandpolito.landpayment.repository

import io.github.kotlandpolito.landpayment.models.Transaction
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Flux

interface TransactionRepository : ReactiveMongoRepository<Transaction, String> {

    fun findByUserId(userId: Long): Flux<Transaction>

}