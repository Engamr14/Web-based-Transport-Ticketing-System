package io.github.kotlandpolito.landpayment.service

import io.github.kotlandpolito.landpayment.models.PaymentRequest
import io.github.kotlandpolito.landpayment.models.Transaction
import io.github.kotlandpolito.landpayment.models.TransactionStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class MockBankService(
    private val transactionService: TransactionService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    suspend fun attemptTransaction(paymentRequest: PaymentRequest): Transaction
    {
        val transaction: Transaction = transactionService.createTransaction(paymentRequest)
        logger.info("transaction initial state: {}", transaction)


        // Simulate delay of calling a external service
        runBlocking {
            delay(10000)
        }

        // Flip a coin to see if transaction is successful or not, then update
        val random = Math.random()

        // Update with simulated result
        val result = if (random < 0.5) {
            TransactionStatus.SUCCESSFUL
        } else {
            TransactionStatus.FAILED
        }

        return transactionService.updateTransactionStatus(transaction.id!!, result)
    }

}