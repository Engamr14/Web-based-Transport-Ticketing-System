package io.github.kotlandpolito.landticketcatalogue.consumers

import com.netflix.appinfo.InstanceInfo
import com.netflix.discovery.EurekaClient
import io.github.kotlandpolito.landticketcatalogue.model.*
import io.github.kotlandpolito.landticketcatalogue.repository.OrderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.TemporalAdjusters

@Component
class PaymentOutcomesConsumer(val orderRepository:OrderRepository,
                              @Autowired private val eurekaClient: EurekaClient) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = ["\${kafka.topics.payment-outcomes}"], groupId = "ppr")
    fun listenForPaymentRequests(consumerRecord: ConsumerRecord<Any, Any>, ack: Acknowledgment){
        ack.acknowledge()

        val paymentOutcome:PaymentOutcome = consumerRecord.value() as PaymentOutcome
        logger.info("PaymentOutcomesConsumer received: {}", paymentOutcome)

        val scope = CoroutineScope(Job())

        scope.launch {
            // retrieve orderId from kafka response
            val orderId = paymentOutcome.orderId

            val order = orderRepository.findById(orderId).awaitSingle()
            logger.info("Initial state of order: {}", order)
            // update the order(COMPLETED vs DENIED)
            if (paymentOutcome.paymentSuccessful){
                order.status = OrderStatus.COMPLETED
            } else {
                order.status = OrderStatus.DENIED
            }
            orderRepository.save(order).awaitSingle()
            logger.info("Updated order: {}", order)
            if (paymentOutcome.paymentSuccessful){
                // we should add the purchased tickets in the TravelerService
                val requestedTicket = order.ticketId
                val hour: Long = 60*60*1000
                val day: Long = 24*hour
                val duration = when (requestedTicket.type) {
                    AvailableTicketType.ORDINAL -> 2*hour
                    AvailableTicketType.DAILY_PASS -> day
                    AvailableTicketType.WEEKEND_PASS -> 2*day
                    AvailableTicketType.MONTHLY_PASS -> 30*day
                    AvailableTicketType.YEARLY_PASS -> 365*day
                }
                val validfrom = when (requestedTicket.type) {
                    AvailableTicketType.ORDINAL -> LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)*1000
                    AvailableTicketType.WEEKEND_PASS -> LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY)).toEpochDay()
                    else -> LocalDate.now().toEpochDay()
                }
                val acquisitionRequest = AcquiredTicketsRequest(order.numberOfTickets.toLong(), validfrom, duration, requestedTicket.zid, requestedTicket.type.toString(), null)
                val userId = order.userId
                logger.info("Prepared a request for TravelerService: {}", acquisitionRequest)

                // this token has role ADMIN with no expiration
                val adminToken = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTUxNjIzOTAyMiwiZXhwIjoxNTE2MjM5MDIyMCwicm9sZSI6IkFETUlOIiwidXNlci1pZCI6IjEifQ.J0lLz25ZSo4GcGfT3YX3rr14YB11pibMUZUk-KPN-NM"

                val travelerService: InstanceInfo = eurekaClient
                    .getApplication("LAND-TRAVELER-SERVICE")
                    .instances[0]

                WebClient.create("http://${travelerService.hostName}:${travelerService.port}")   // this should point to the TravelerService
                    .post()
                    .uri("/admin/traveler/${userId}/tickets")
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", adminToken)
                    .bodyValue(acquisitionRequest)
                    .retrieve()
                    .bodyToMono(Void::class.java).awaitSingleOrNull()
            }

        }

    }



}
