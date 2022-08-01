package io.github.kotlandpolito.landticketcatalogue.service

import com.netflix.appinfo.InstanceInfo
import com.netflix.discovery.EurekaClient
import io.github.kotlandpolito.landticketcatalogue.model.*
import io.github.kotlandpolito.landticketcatalogue.repository.OrderRepository
import io.github.kotlandpolito.landticketcatalogue.repository.TicketRepository
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import java.time.*
import java.util.*

@Service
class OrderService(private val orderRepository: OrderRepository,
                   private val ticketRepository: TicketRepository,
                   @Value("\${kafka.topics.payment-requests}") val paymentRequestsTopic: String,
                   @Autowired private val producerTemplate: KafkaTemplate<String, PaymentRequest>,
                   @Autowired private val eurekaClient: EurekaClient
                   ) {

    private val logger = LoggerFactory.getLogger(javaClass)

    suspend fun getAll() = orderRepository.findAll().asFlow()

    suspend fun getAllByUserId(userId: Long) = orderRepository.findAllByUserId(userId).asFlow()

    suspend fun getByUserIdAndId(userId: Long, orderId: String) = orderRepository.findByUserIdAndId(userId, orderId).awaitSingleOrNull()

    suspend fun getById(orderId: String) = orderRepository.findById(orderId).awaitSingleOrNull()

    suspend fun createOrder(ticketId: String, orderRequest: OrderRequest, userId: Long, userToken: String): String? {
        val requestedTicket = ticketRepository.findById(ticketId).awaitSingleOrNull() ?: return null

        val amount: Double = (orderRequest.numberOfTickets * requestedTicket.price).toDouble()

        // if there is an age limitation, contact the TravelerService to retrieve the age (impersonating the user)

        if (requestedTicket.minAge > 0 || requestedTicket.maxAge > 0) {

            val travelerService: InstanceInfo = eurekaClient
                .getApplication("land-traveler-service")
                .instances[0]

            val userDetails = WebClient.create("http://${travelerService.hostName}:${travelerService.port}") // this should point to the TravelerService
                .get().uri("/my/profile").accept(MediaType.APPLICATION_JSON)
                .header("Authorization", userToken)
                .retrieve().awaitBody<UserDetails>()
            val birthday = LocalDate.parse(userDetails.date_of_birth)
            val age = Period.between(birthday, LocalDate.now())
            if (requestedTicket.minAge > 0 && age.years < requestedTicket.minAge){
                return null
            }
            if (requestedTicket.maxAge > 0 && age.years > requestedTicket.maxAge){
                return null
            }
        }

        val newOrder = Order(null, userId, orderRequest.numberOfTickets, requestedTicket, OrderStatus.PENDING)
        val orderId = orderRepository.save(newOrder).awaitSingle().id

        // if we are here, everything is fine, let's store the order, then post a payment request to kafka,
        // and return the order with pending status
        val paymentRequest = PaymentRequest(orderId=orderId!!, userId=userId, amount=amount,
                                            ticketId=orderRequest.ticketId,
                                            creditCardNumber=orderRequest.creditCardNumber,
                                            creditCardExpirationDate=orderRequest.creditCardExpirationDate,
                                            creditCardCVV=orderRequest.creditCardCVV,
                                            cardHolder=orderRequest.cardHolder)

        logger.info("Creating payment request: {}", paymentRequest)
        val message: Message<PaymentRequest> = MessageBuilder
            .withPayload(paymentRequest)
            .setHeader(KafkaHeaders.TOPIC, paymentRequestsTopic)
            .build()
        producerTemplate.send(message)
        logger.info("Message sent with success")

        return orderId
    }

}