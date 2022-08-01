package io.github.kotlandpolito.landpayment.service

import io.github.kotlandpolito.landpayment.models.PaymentRequest
import io.github.kotlandpolito.landpayment.models.Transaction
import io.github.kotlandpolito.landpayment.models.TransactionStatus
import io.github.kotlandpolito.landpayment.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.format.DateTimeFormatter

@Service
class TransactionService(
    private val transactionRepository: TransactionRepository,
) {
    fun findAll(): Flow<Transaction> = transactionRepository.findAll().asFlow()

    fun findAllByUserId(userId: Long): Flow<Transaction> = transactionRepository.findByUserId(userId).asFlow()

    suspend fun createTransaction(paymentRequest: PaymentRequest): Transaction
    {
        val t = Transaction(
            amount = paymentRequest.amount,
            orderId = paymentRequest.orderId,
            userId =paymentRequest.userId,
            status = TransactionStatus.PENDING,
            start_timestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        )

        return transactionRepository.save(t).awaitSingle()
    }

    suspend fun updateTransactionStatus(id: String, newStatus: TransactionStatus): Transaction {
        val transaction = transactionRepository.findById(id).awaitSingle()
        transaction.status = newStatus
        transaction.end_timestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        return transactionRepository.save(transaction).awaitSingle()
    }

}