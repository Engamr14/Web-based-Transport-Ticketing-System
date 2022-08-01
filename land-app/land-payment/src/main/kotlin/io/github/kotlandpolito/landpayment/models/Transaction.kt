package io.github.kotlandpolito.landpayment.models

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import java.time.format.DateTimeFormatter

@Document
data class Transaction(
    @Id val id: String? = null,
    val userId: Long,
    val orderId: String,
    val amount: Double,
    var status: TransactionStatus = TransactionStatus.PENDING,
    val start_timestamp:String = DateTimeFormatter.ISO_INSTANT.format(Instant.now()),
    var end_timestamp:String? = null
)

enum class TransactionStatus {
    SUCCESSFUL, PENDING, FAILED
}


