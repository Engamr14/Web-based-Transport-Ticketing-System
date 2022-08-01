package io.github.kotlandpolito.landpayment.controllers

import io.github.kotlandpolito.landpayment.models.PaymentOutcome
import io.github.kotlandpolito.landpayment.models.PaymentRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/test-payment-request")
class PaymentRequestTestController(
    @Value("\${kafka.topics.payment-requests}") val topic: String,
    @Autowired
    private val producerTemplate: KafkaTemplate<String, PaymentRequest>
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @PostMapping
    fun post(@Validated @RequestBody paymentRequest: PaymentRequest): ResponseEntity<Any> {
        return try {
            log.info("Creating payment request: {}", paymentRequest)
            val message: Message<PaymentRequest> = MessageBuilder
                .withPayload(paymentRequest)
                .setHeader(KafkaHeaders.TOPIC, topic)
                .setHeader("X-Custom-Header", "Custom header here")
                .build()
            producerTemplate.send(message)
            log.info("Message sent with success")
            ResponseEntity.ok().build()
        } catch (e: Exception) {
            log.error("Exception: {}",e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error to send message")
        }
    }
}

@RestController
@RequestMapping("/test-payment-outcome")
class PaymentOutcomeTestController(
    @Value("\${kafka.topics.payment-outcomes}") val topic: String,
    @Autowired
    private val producerTemplate: KafkaTemplate<String, PaymentOutcome>
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @PostMapping
    fun post(@Validated @RequestBody paymentOutcome: PaymentOutcome): ResponseEntity<Any> {
        return try {
            log.info("Creating payment outcome: {}", paymentOutcome)
            val message: Message<PaymentOutcome> = MessageBuilder
                .withPayload(paymentOutcome)
                .setHeader(KafkaHeaders.TOPIC, topic)
                .setHeader("X-Custom-Header", "Custom header here")
                .build()
            producerTemplate.send(message)
            log.info("Message sent with success")
            ResponseEntity.ok().build()
        } catch (e: Exception) {
            log.error("Exception: {}",e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error to send message")
        }
    }
}