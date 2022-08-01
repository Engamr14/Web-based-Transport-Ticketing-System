package io.github.kotlandpolito.lab3

import io.github.kotlandpolito.lab3.activation.ActivationRepository
import io.github.kotlandpolito.lab3.activation.ActivationRequestDTO
import io.github.kotlandpolito.lab3.activation.PendingActivationDTO
import io.github.kotlandpolito.lab3.user.UserRepository
import io.github.kotlandpolito.lab3.user.UserRequestDTO
import io.github.kotlandpolito.lab3.user.VerifiedUserDTO
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.*

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ApplicationTests {
    companion object {
        @Container
        val postgres = PostgreSQLContainer("postgres:latest")

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
            registry.add("spring.jpa.hibernate.ddl-auto") { "create-drop" }
        }
    }

    @LocalServerPort
    protected var port: Int = 0

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var activationRepository: ActivationRepository

    /**
     * This test checks that the ENTIRE application works when using valid requests
     * */
    @Test
    fun testCorrectUsageOfEntireApplication() {
        /* Client-side: send valid registration request */
        val baseUrl = "http://localhost:$port"
        val user = UserRequestDTO(
            username = "somename",
            email = "kotland.polito+correctusagetest@gmail.com",
            password = "Secret!Password1"
        )
        val registrationRequest = HttpEntity(user)
        val registrationResponse = restTemplate.postForEntity<PendingActivationDTO>(
            "$baseUrl/user/register", registrationRequest
        )
        assert(registrationResponse.statusCode == HttpStatus.ACCEPTED)

        /* Client-side: successful registration should return a PendingActivationDTO with provisional id and email */
        val registrationResponseDTO: PendingActivationDTO? = registrationResponse.body
        Assertions.assertNotNull(registrationResponseDTO?.provisional_id)
        Assertions.assertNotNull(registrationResponseDTO?.email)
        Assertions.assertEquals(user.email, registrationResponseDTO?.email)

        /* Server-side: check that the user exists and its fields are correct */
        var userInDatabase = userRepository.findByEmail(user.email)
        Assertions.assertNotNull(userInDatabase)
        Assertions.assertEquals(user.username, userInDatabase?.username)
        Assertions.assertEquals(user.email, userInDatabase?.email)
        if (userInDatabase != null) {
            Assertions.assertFalse(userInDatabase.isActive)
        }

        /* Server-side: check that the activation has been added and its fields are correct */
        // id, user, attemptCounter, code, expirationDate
        val responseUUID = UUID.fromString(registrationResponseDTO?.provisional_id)
        var optActivationInDatabase = activationRepository.findById(responseUUID)
        Assertions.assertTrue(optActivationInDatabase.isPresent)
        if (optActivationInDatabase.isPresent) {
            val activationInDatabase = optActivationInDatabase.get()
            Assertions.assertNotNull(activationInDatabase)
            Assertions.assertEquals(5, activationInDatabase.attemptCounter)
            Assertions.assertEquals(userInDatabase?.id, activationInDatabase.user.id)
        }

        // let's cheat a bit and retrieve the activation code from the database
        val activationCode = optActivationInDatabase.get().code

        /* Client-side: send valid activation request */
        val activation = ActivationRequestDTO(
            provisional_id = responseUUID.toString(),
            code = activationCode,
        )
        val activationRequest = HttpEntity(activation)
        val activationResponse = restTemplate.postForEntity<VerifiedUserDTO>(
            "$baseUrl/user/verify", activationRequest
        )
        assert(activationResponse.statusCode == HttpStatus.CREATED)

        /* Client-side: successful registration should return a VerifiedUserDTO with id, username and email */
        val activationResponseDTO: VerifiedUserDTO? = activationResponse.body
        Assertions.assertNotNull(activationResponseDTO?.id)
        Assertions.assertNotNull(activationResponseDTO?.username)
        Assertions.assertNotNull(activationResponseDTO?.email)
        Assertions.assertEquals(userInDatabase?.id, activationResponseDTO?.id)
        Assertions.assertEquals(user.username, activationResponseDTO?.username)
        Assertions.assertEquals(user.email, activationResponseDTO?.email)

        /* Server-side: check that the activation has been removed and the user is now active */
        optActivationInDatabase = activationRepository.findById(responseUUID)
        Assertions.assertFalse(optActivationInDatabase.isPresent)
        userInDatabase = userRepository.findByEmail(user.email)
        Assertions.assertNotNull(userInDatabase)
        if (userInDatabase != null) {
            Assertions.assertTrue(userInDatabase.isActive)
        }
    }

    /**
     * This test checks whether the user sent an empty username in the registration request
     * */
    @Test
    fun testEmptyUsername() {
        /* Client-side: send registration request with empty username */
        val baseUrl = "http://localhost:$port"
        val user = UserRequestDTO(
            username = "", email = "kotland.polito+emptyusername@gmail.com",
            password = "Secret!Password1"
        )
        val registrationRequest = HttpEntity(user)
        val registrationResponse = restTemplate.postForEntity<Unit>(
            "$baseUrl/user/register", registrationRequest
        )
        assert(registrationResponse.statusCode == HttpStatus.BAD_REQUEST)
    }

    /**
     * This test checks whether the user sent an empty password in the registration request
     * */
    @Test
    fun testEmptyPassword() {
        /* Client-side: send registration request with empty password */
        val baseUrl = "http://localhost:$port"
        val user = UserRequestDTO(
            username = "emptypassword", email = "kotland.polito+emptypassword@gmail.com",
            password = ""
        )
        val registrationRequest = HttpEntity(user)
        val registrationResponse = restTemplate.postForEntity<Unit>(
            "$baseUrl/user/register", registrationRequest
        )
        assert(registrationResponse.statusCode == HttpStatus.BAD_REQUEST)
    }

    /**
     * This test checks whether the user sent a too short password in the registration request
     * */
    @Test
    fun testShortPassword() {
        /* Client-side: send registration request with short password */
        val baseUrl = "http://localhost:$port"
        val user = UserRequestDTO(
            username = "shortpassword", email = "kotland.polito+shortpassword@gmail.com",
            password = "short"
        )
        val registrationRequest = HttpEntity(user)
        val registrationResponse = restTemplate.postForEntity<Unit>(
            "$baseUrl/user/register", registrationRequest
        )
        assert(registrationResponse.statusCode == HttpStatus.BAD_REQUEST)
    }

    /**
     * This test checks whether the user sent a password without numbers in the registration request
     * */
    @Test
    fun testInvalidPasswordNoNumbers() {
        /* Client-side: send registration request with a password without numbers */
        val baseUrl = "http://localhost:$port"
        val user = UserRequestDTO(
            username = "alphapassword", email = "kotland.polito+alphapassword@gmail.com",
            password = "Secret!Password"
        )
        val registrationRequest = HttpEntity(user)
        val registrationResponse = restTemplate.postForEntity<Unit>(
            "$baseUrl/user/register", registrationRequest
        )
        assert(registrationResponse.statusCode == HttpStatus.BAD_REQUEST)
    }

    /**
     * This test checks whether the user sent a password without special characters in the registration request
     * */
    @Test
    fun testInvalidPasswordNoSpecial() {
        /* Client-side: send registration request with a password without special characters */
        val baseUrl = "http://localhost:$port"
        val user = UserRequestDTO(
            username = "nospecialpassword", email = "kotland.polito+nospecialpassword@gmail.com",
            password = "SecretPassword1"
        )
        val registrationRequest = HttpEntity(user)
        val registrationResponse = restTemplate.postForEntity<Unit>(
            "$baseUrl/user/register", registrationRequest
        )
        assert(registrationResponse.statusCode == HttpStatus.BAD_REQUEST)
    }

    /**
     * This test checks whether the user sent a password with only UPPERCASE characters in the registration request
     * */
    @Test
    fun testInvalidPasswordOnlyUpper() {
        /* Client-side: send registration request with a password with only UPPERCASE characters */
        val baseUrl = "http://localhost:$port"
        val user = UserRequestDTO(
            username = "upperpassword", email = "kotland.polito+upperpassword@gmail.com",
            password = "SECRET!PASSWORD1"
        )
        val registrationRequest = HttpEntity(user)
        val registrationResponse = restTemplate.postForEntity<Unit>(
            "$baseUrl/user/register", registrationRequest
        )
        assert(registrationResponse.statusCode == HttpStatus.BAD_REQUEST)
    }

    /**
     * This test checks whether the user sent a password with only lowercase characters in the registration request
     * */
    @Test
    fun testInvalidPasswordOnlyLower() {
        /* Client-side: send registration request with a password with only UPPERCASE characters */
        val baseUrl = "http://localhost:$port"
        val user = UserRequestDTO(
            username = "lowerpassword", email = "kotland.polito+lowerpassword@gmail.com",
            password = "secret!password1"
        )
        val registrationRequest = HttpEntity(user)
        val registrationResponse = restTemplate.postForEntity<Unit>(
            "$baseUrl/user/register", registrationRequest
        )
        assert(registrationResponse.statusCode == HttpStatus.BAD_REQUEST)
    }

    /**
     * This test checks whether the user sent an empty email in the registration request
     * */
    @Test
    fun testEmptyEmail() {
        /* Client-side: send registration request with empty email */
        val baseUrl = "http://localhost:$port"
        val user = UserRequestDTO(
            username = "emptyemail", email = "",
            password = "SecretPassword1"
        )
        val registrationRequest = HttpEntity(user)
        val registrationResponse = restTemplate.postForEntity<Unit>(
            "$baseUrl/user/register", registrationRequest
        )
        assert(registrationResponse.statusCode == HttpStatus.BAD_REQUEST)
    }

    /**
     * This test checks whether the user sent an invalid email in the registration request
     * */
    @Test
    fun testInvalidEmail() {
        /* Client-side: send registration request with invalid email */
        val baseUrl = "http://localhost:$port"
        val user = UserRequestDTO(
            username = "invalidemail", email = "invalidemail",
            password = "SecretPassword1"
        )
        val registrationRequest = HttpEntity(user)
        val registrationResponse = restTemplate.postForEntity<Unit>(
            "$baseUrl/user/register", registrationRequest
        )
        assert(registrationResponse.statusCode == HttpStatus.BAD_REQUEST)
    }

    /**
     * This test checks whether the user sent a non-existing provisional id in the activation request
     * */
    @Test
    fun testProvisionalIDNotExists() {
        // Make sure we pick a provisional id that truly doesn't exist
        var nonExistingUUID: UUID
        do {
            nonExistingUUID = UUID.randomUUID()
        } while (activationRepository.existsById(nonExistingUUID))

        /* Client-side: send activation request with non-existing provisional id */
        val baseUrl = "http://localhost:$port"
        val activation = ActivationRequestDTO(
            provisional_id = nonExistingUUID.toString(),
            code = "123456",
        )

        val request = HttpEntity(activation)
        val response = restTemplate.postForEntity<Unit>(
            "$baseUrl/user/verify", request
        )
        assert(response.statusCode == HttpStatus.NOT_FOUND)
    }

    /**
     * This test checks whether the user sent a wrong code in the activation request
     * */
    @Test
    fun testWrongActivationCode() {
        /* Client-side: send valid registration request */
        val baseUrl = "http://localhost:$port"
        val user = UserRequestDTO(
            username = "wrongactivationcode", email = "kotland.polito+wrongactivationcode@gmail.com",
            password = "Secret!Password1"
        )
        val registrationRequest = HttpEntity(user)
        val registrationResponse = restTemplate.postForEntity<PendingActivationDTO>(
            "$baseUrl/user/register", registrationRequest
        )
        assert(registrationResponse.statusCode == HttpStatus.ACCEPTED)

        val validProvisionalId = UUID.fromString(registrationResponse.body?.provisional_id)

        // let's cheat a bit and retrieve the user id and activation code from the database
        val correctUserId = activationRepository.findById(validProvisionalId).get().user.id
        Assertions.assertNotNull(correctUserId)
        val correctActivationCode = activationRepository.findById(validProvisionalId).get().code
        val wrongActivationCode = correctActivationCode.toCharArray().shuffle().toString()

        val activation = ActivationRequestDTO(
            provisional_id = validProvisionalId.toString(),
            code = wrongActivationCode,
        )

        for (i in 1..4) {
            /* Client-side: send activation request with wrong activation code */
            val activationRequest = HttpEntity(activation)
            val activationResponse = restTemplate.postForEntity<Unit>(
                "$baseUrl/user/verify", activationRequest
            )
            assert(activationResponse.statusCode == HttpStatus.NOT_FOUND)

            /* Server-side: check that the attempt counter is being lowered */
            Assertions.assertEquals(5 - i, activationRepository.findById(validProvisionalId).get().attemptCounter)
        }

        // here the attempt counter should be 1, let's do one last push
        /* Client-side: send activation request with wrong activation code */
        val activationRequest = HttpEntity(activation)
        val activationResponse = restTemplate.postForEntity<Unit>(
            "$baseUrl/user/verify", activationRequest
        )
        assert(activationResponse.statusCode == HttpStatus.NOT_FOUND)

        /* Server-side: check that the activation and user have been removed */
        Assertions.assertFalse(activationRepository.findById(validProvisionalId).isPresent)
        if (correctUserId != null) {
            Assertions.assertFalse(userRepository.findById(correctUserId).isPresent)
        }
    }

}