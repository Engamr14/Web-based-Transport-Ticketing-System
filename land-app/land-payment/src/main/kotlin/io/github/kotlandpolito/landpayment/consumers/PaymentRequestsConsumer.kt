package io.github.kotlandpolito.landpayment.consumers

import io.github.kotlandpolito.landpayment.models.PaymentOutcome
import io.github.kotlandpolito.landpayment.models.PaymentRequest
import io.github.kotlandpolito.landpayment.models.TransactionStatus
import io.github.kotlandpolito.landpayment.service.MockBankService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component

@Component
class PaymentRequestsConsumer(val bankService: MockBankService,
                              @Value("\${kafka.topics.payment-outcomes}") val outcomesTopic: String,
                              @Autowired private val producerTemplate: KafkaTemplate<String, PaymentOutcome>) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = ["\${kafka.topics.payment-requests}"], groupId = "ppr")
    fun listenForPaymentRequests(consumerRecord: ConsumerRecord<Any, Any>, ack: Acknowledgment){
        logger.info("PaymentRequestsConsumer received message: {}", consumerRecord)
        ack.acknowledge()

        val paymentRequest:PaymentRequest = consumerRecord.value() as PaymentRequest

        val scope = CoroutineScope(Job())
        scope.launch {
            val transaction = bankService.attemptTransaction(paymentRequest)
            logger.info("transaction final state: {}", transaction)

            // Prepare & publish paymentOutcome to payment-outcomes topic
            val paymentOutcome = PaymentOutcome(orderId = paymentRequest.orderId,
                paymentSuccessful = (transaction.status == TransactionStatus.SUCCESSFUL))


            logger.info("publishing payment outcome: {}", paymentOutcome)
            val message: Message<PaymentOutcome> = MessageBuilder
                .withPayload(paymentOutcome)
                .setHeader(KafkaHeaders.TOPIC, outcomesTopic)
                .build()
            producerTemplate.send(message)
            logger.info("Message sent with success")
        }

    }

}
