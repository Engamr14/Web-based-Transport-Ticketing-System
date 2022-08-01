package io.github.kotlandpolito.landticketcatalogue.model

import com.fasterxml.jackson.annotation.JsonProperty

data class PaymentRequest(
    @JsonProperty("orderId")
    val orderId: String,

    @JsonProperty("userId")
    val userId: Long,

    @JsonProperty("amount")
    val amount: Double,

    @JsonProperty("ticketId")
    val ticketId: String,

    @JsonProperty("creditCardNumber")
    val creditCardNumber: String,

    @JsonProperty("creditCardExpirationDate")
    val creditCardExpirationDate: String,

    @JsonProperty("creditCardCVV")
    val creditCardCVV: String,

    @JsonProperty("cardHolder")
    val cardHolder: String
){
    constructor() : this("",0L,0.0,"","","","","")
}

