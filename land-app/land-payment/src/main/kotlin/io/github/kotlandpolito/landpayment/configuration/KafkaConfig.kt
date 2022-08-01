package io.github.kotlandpolito.landpayment.configuration

import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.NewTopic
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.KafkaAdmin

@Configuration
class KafkaConfig(
    @Value("\${kafka.bootstrapAddress}")
    private val servers: String,
    @Value("\${kafka.topics.payment-requests}")
    private val _paymentRequestsTopic: String,
    @Value("\${kafka.topics.payment-outcomes}")
    private val _paymentOutcomesTopic: String
) {

    @Bean
    fun kafkaAdmin(): KafkaAdmin {
        val configs: MutableMap<String, Any?> = HashMap()
        configs[AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG] = servers
        return KafkaAdmin(configs)
    }

    @Bean
    fun paymentRequestsTopic(): NewTopic {
        return NewTopic(_paymentRequestsTopic, 1, 1.toShort())
    }

    @Bean
    fun paymentOutcomesTopic(): NewTopic {
        return NewTopic(_paymentOutcomesTopic, 1, 1.toShort())
    }
}