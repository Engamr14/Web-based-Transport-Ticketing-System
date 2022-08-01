package io.github.kotlandpolito.landticketcatalogue.serialization

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.kotlandpolito.landticketcatalogue.model.PaymentOutcome
import org.apache.kafka.common.errors.SerializationException
import org.apache.kafka.common.serialization.Serializer
import org.slf4j.LoggerFactory


class PaymentOutcomeSerializer : Serializer<PaymentOutcome> {
    private val objectMapper = ObjectMapper()
    private val log = LoggerFactory.getLogger(javaClass)

    override fun serialize(topic: String?, data: PaymentOutcome?): ByteArray? {
        log.info("Serializing...")
        return objectMapper.writeValueAsBytes(
            data ?: throw SerializationException("Error when serializing PaymentRequest to ByteArray[]")
        )
    }

    override fun close() {}
}
