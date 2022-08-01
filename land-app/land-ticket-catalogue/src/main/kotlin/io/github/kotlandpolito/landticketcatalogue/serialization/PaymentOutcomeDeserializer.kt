package io.github.kotlandpolito.landticketcatalogue.serialization

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.kotlandpolito.landticketcatalogue.model.PaymentOutcome
import org.apache.kafka.common.errors.SerializationException
import org.apache.kafka.common.serialization.Deserializer
import org.slf4j.LoggerFactory
import kotlin.text.Charsets.UTF_8


class PaymentOutcomeDeserializer : Deserializer<PaymentOutcome> {
    private val objectMapper = ObjectMapper()
    private val log = LoggerFactory.getLogger(javaClass)

    override fun deserialize(topic: String?, data: ByteArray?): PaymentOutcome? {
        log.info("Deserializing...")
        return objectMapper.readValue(
            String(
                data ?: throw SerializationException("Error when deserializing byte[] to Product"), UTF_8
            ), PaymentOutcome::class.java
        )
    }

    override fun close() {}

}
