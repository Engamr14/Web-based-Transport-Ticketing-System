package io.github.kotlandpolito.lab3


import io.github.kotlandpolito.lab3.activation.PendingActivationDTO
import io.github.kotlandpolito.lab3.user.*
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.boot.test.web.client.postForObject
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import java.util.*
import kotlin.collections.LinkedHashMap

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("rate-limit-test")
class RateLimitingTest {
    @LocalServerPort
    protected var port: Int = 0

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Test
    fun testSequentialRegisterRequests() {
        val baseUrl = "http://localhost:$port"

        val user = UserRequestDTO(
            username = UUID.randomUUID().toString(),
            email = UUID.randomUUID().toString() + "@email.com",
            password = "Secret!Password1"
        )

        val request = HttpEntity(user)
        val response = restTemplate.postForEntity<PendingActivationDTO>(
            "$baseUrl/user/register", request
        )
        assert(response.statusCode == HttpStatus.ACCEPTED)

        // Successful registration should return a user DTO with provisional id and email
        val responseDTO: PendingActivationDTO? = response.body
        assertNotNull(responseDTO?.provisional_id)
        assertNotNull(responseDTO?.email)

        // Subsequent request should be rejected due to exceeding rate limit
        val errorResponse = restTemplate.postForObject<Any>(
            "$baseUrl/user/register", HttpEntity(user)
        )

        // get value with the key "status" in error_response
        val status = (errorResponse as LinkedHashMap<*, *>)["status"]
        assert(status == HttpStatus.TOO_MANY_REQUESTS.value())
    }
}