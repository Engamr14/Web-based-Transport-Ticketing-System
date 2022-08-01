package io.github.kotlandpolito.landticketcatalogue.configuration

import io.github.kotlandpolito.landticketcatalogue.model.PaymentRequest
import io.github.kotlandpolito.landticketcatalogue.serialization.PaymentRequestSerializer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory


@Configuration
class KafkaProducerConfig(
    @Value("\${kafka.bootstrapAddress}")
    private val servers: String
) {
    @Bean
    fun paymentRequestProducerFactory(): ProducerFactory<String, PaymentRequest> {
        val configProps: MutableMap<String, Any> = HashMap()
        configProps[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = servers
        configProps[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        configProps[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = PaymentRequestSerializer::class.java
        return DefaultKafkaProducerFactory(configProps)
    }

    @Bean
    fun paymentRequestTemplate(): KafkaTemplate<String, PaymentRequest> {
        return KafkaTemplate(paymentRequestProducerFactory())
    }
}
