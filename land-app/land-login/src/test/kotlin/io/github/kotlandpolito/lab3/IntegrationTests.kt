package io.github.kotlandpolito.lab3

import io.github.kotlandpolito.lab3.activation.ActivationDTO
import io.github.kotlandpolito.lab3.activation.ActivationRepository
import io.github.kotlandpolito.lab3.email.InvalidEmailAddressException
import io.github.kotlandpolito.lab3.user.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.*

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class IntegrationTests {
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

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var activationRepository: ActivationRepository

    //User service

    @Autowired
    lateinit var userService: LANDUserService

    //tests for startUserRegistration

    @Test
    fun testUsernameAlreadyExists() {
        Assertions.assertThrows(UsernameAlreadyUsedException::class.java) {
            userService.startUserRegistration(
                "nunzio",
                "V4lid.Passw0rd",
                "kotland.polito+usernamealreadyexists@gmail.com"
            )
            userService.startUserRegistration(
                "nunzio",
                "V4lid.Passw0rd",
                "kotland.polito+usernamealreadyexists@gmail.com"
            )
        }
    }

    @Test
    fun testEmailAlreadyExists() {
        Assertions.assertThrows(EmailAddressAlreadyUsedException::class.java) {
            userService.startUserRegistration("prova1", "V4lid.Passw0rd", "kotland.polito+emailalreadyexists@gmail.com")
            userService.startUserRegistration("prova2", "V4lid.Passw0rd", "kotland.polito+emailalreadyexists@gmail.com")
        }
    }

    @Test
    fun testEmptyUsername() {
        Assertions.assertThrows(InvalidUsernameException::class.java) {
            userService.startUserRegistration("", "V4lid.Passw0rd", "kotland.polito+emptyusername@gmail.com")
        }
    }

    @Test
    fun testEmptyEmail() {
        Assertions.assertThrows(InvalidEmailAddressException::class.java) {
            userService.startUserRegistration("nunzio", "V4lid.Passw0rd", "")
        }
    }

    @Test
    fun testValidUserRegistration() {
        val activationId: ActivationDTO? =
            userService.startUserRegistration("nunzio2", "V4lid.Passw0rd", "kotland.polito+validuser@gmail.com")
        org.assertj.core.api.Assertions.assertThat(activationId).isNotNull
    }

    //tests for verifyUserActivation

    @Test
    fun testInvalidActivationId() {
        Assertions.assertNull(userService.verifyUserActivation(UUID(0L, 0L), "justarandomactivationcode"))
    }

    @Test
    fun testValidActivationId() {
        val activationDTO: ActivationDTO? =
            userService.startUserRegistration("nunzio", "V4lid.Passw0rd", "kotland.polito+validactivation@gmail.com")
        if (activationDTO != null) {
            val id = activationDTO.id
            Assertions.assertNotNull(id)
        }
    }

}