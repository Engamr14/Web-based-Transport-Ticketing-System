package io.github.kotlandpolito.lab3

import io.github.kotlandpolito.lab3.activation.ActivationRequestDTO
import io.github.kotlandpolito.lab3.activation.PendingActivationDTO
import io.github.kotlandpolito.lab3.user.*
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import java.util.*

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("controller-test")
class ControllerUnitTests {
    @LocalServerPort
    protected var port: Int = 0

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Test
    fun registerUser() {
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

    }

    @Test
    fun verifyUser() {
        val baseUrl = "http://localhost:$port"

        val activation = ActivationRequestDTO(
            provisional_id = "00000000-0000-0000-0000-000000000000",
            code = "123456",
        )

        val request = HttpEntity(activation)
        val response = restTemplate.postForEntity<VerifiedUserDTO>(
            "$baseUrl/user/verify", request
        )
        assert(response.statusCode == HttpStatus.CREATED)

        // Successful registration should return DTO with id, username and email
        val responseDTO: VerifiedUserDTO? = response.body
        assertNotNull(responseDTO?.id)
        assertNotNull(responseDTO?.username)
        assertNotNull(responseDTO?.email)
    }
}