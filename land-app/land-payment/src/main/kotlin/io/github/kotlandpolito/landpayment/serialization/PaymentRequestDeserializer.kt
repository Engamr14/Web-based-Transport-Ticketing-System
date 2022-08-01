package io.github.kotlandpolito.landpayment.serialization

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.kotlandpolito.landpayment.models.PaymentRequest
import org.apache.kafka.common.errors.SerializationException
import org.apache.kafka.common.serialization.Deserializer
import org.slf4j.LoggerFactory
import kotlin.text.Charsets.UTF_8


class PaymentRequestDeserializer : Deserializer<PaymentRequest> {
    private val objectMapper = ObjectMapper()
    private val log = LoggerFactory.getLogger(javaClass)

    override fun deserialize(topic: String?, data: ByteArray?): PaymentRequest? {
        log.info("Deserializing...")
        return objectMapper.readValue(
            String(
                data ?: throw SerializationException("Error when deserializing byte[] to Product"), UTF_8
            ), PaymentRequest::class.java
        )
    }

    override fun close() {}

}
