package io.github.kotlandpolito.landticketcatalogue.model
import com.fasterxml.jackson.annotation.JsonProperty

data class PaymentOutcome(
    @JsonProperty("orderId")
    val orderId: String,

    @JsonProperty("paymentSuccessful")
    val paymentSuccessful: Boolean
){
    constructor() : this("", false)
}

