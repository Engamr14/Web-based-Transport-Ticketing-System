package io.github.kotlandpolito.lab4traveler

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.*

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class ControllerTests {
    @LocalServerPort
    protected var port: Int = 0

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Test
    fun getUserProfile() {
        val baseUrl = "http://localhost:$port"

        val bearer = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJKb2huIiwiaWF0IjoxNTE2MjM5MDIyLCJyb2xlIjoiQURNSU4ifQ.7zEKGr4yeXPU8fYxlH1Aqfoem-bFminSKiSsApq-zS4"

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers["Authorization"] = bearer
        val requestEntity = HttpEntity<String>(headers)

        restTemplate.exchange<String>(
            "$baseUrl/my/profile",
            HttpMethod.GET,
            requestEntity,
            String::class.java
        ).also {
            assertNotNull(it)
            assertEquals(HttpStatus.OK, it.statusCode)
        }

    }

    @Test
    fun getTravelersFail() {
        // Test non-admin user trying to access admin route

        val baseUrl = "http://localhost:$port"

        val bearer = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJBbGljZSIsImlhdCI6MTUxNjIzOTAyMiwicm9sZSI6IkNVU1RPTUVSIn0.JJhnF0QaoAJ8A4gNPl0xgOTLb9RRsHA3AVYQxyxB6zU"

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers["Authorization"] = bearer
        val requestEntity = HttpEntity<String>(headers)

        restTemplate.exchange<String>(
            "$baseUrl/admin/travelers",
            HttpMethod.GET,
            requestEntity,
            String::class.java
        ).also {
            assertNotNull(it)
            assertEquals(HttpStatus.FORBIDDEN, it.statusCode)
        }

    }


    @Test
    fun getTravelersSuccess() {
        // Test admin user trying to access admin route

        val baseUrl = "http://localhost:$port"

        val bearer = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJKb2huIiwiaWF0IjoxNTE2MjM5MDIyLCJyb2xlIjoiQURNSU4ifQ.7zEKGr4yeXPU8fYxlH1Aqfoem-bFminSKiSsApq-zS4"

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers["Authorization"] = bearer
        val requestEntity = HttpEntity<String>(headers)

        restTemplate.exchange<String>(
            "$baseUrl/admin/travelers",
            HttpMethod.GET,
            requestEntity,
            String::class.java
        ).also {
            assertNotNull(it)
            assertEquals(HttpStatus.OK, it.statusCode)
        }
    }


    @Test
    fun buyUserTickets() {
        // Test non-admin user trying to access admin route

        val baseUrl = "http://localhost:$port"

        val bearer = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJBbGljZSIsImlhdCI6MTUxNjIzOTAyMiwicm9sZSI6IkNVU1RPTUVSIn0.JJhnF0QaoAJ8A4gNPl0xgOTLb9RRsHA3AVYQxyxB6zU"

        val body = TicketPurchaseRequestDTO(
           cmd="buy_tickets", quantity=3, zones="ABC"
        )

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers["Authorization"] = bearer

        val request = HttpEntity(body, headers)
        val response = restTemplate.postForEntity<Any>(
            "$baseUrl/my/tickets", request
        )
        assertNotNull(response)
        assert(response.statusCode == HttpStatus.OK)

        @Suppress("UNCHECKED_CAST")
        val tickets:List<TicketPurchasedDTO> = response.body as List<TicketPurchasedDTO>

        assertNotNull(tickets)
        assertEquals(3, tickets.size)
    }

}