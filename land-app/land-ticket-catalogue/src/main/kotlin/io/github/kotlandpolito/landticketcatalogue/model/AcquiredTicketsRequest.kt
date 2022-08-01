package io.github.kotlandpolito.landticketcatalogue.model

data class AcquiredTicketsRequest(
    val quantity: Long,
    val validfrom: Long,
    val duration: Long,
    val zid: String,
    val type: String,
    val jws: String?
)