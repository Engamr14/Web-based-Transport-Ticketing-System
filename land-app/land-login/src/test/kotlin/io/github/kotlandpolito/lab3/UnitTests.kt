package io.github.kotlandpolito.lab3

import io.github.kotlandpolito.lab3.activation.LANDActivation
import io.github.kotlandpolito.lab3.activation.toActivationDTO
import io.github.kotlandpolito.lab3.email.InvalidEmailAddressException
import io.github.kotlandpolito.lab3.email.LANDEmailService
import io.github.kotlandpolito.lab3.user.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.*

@SpringBootTest
class UnitTests {

    @Autowired
    lateinit var emailService: LANDEmailService

    //DTOs

    @Test
    fun testConvertUserToDTO() {
        val dami = LANDUser("ozerodb", "lmao", "ozerodb@outlook.com")
        val damiDTO = dami.toUserDTO()
        assertThat(dami.username).isEqualTo(damiDTO.username)
        assertThat(dami.hash).isEqualTo(damiDTO.password)
        assertThat(dami.email).isEqualTo(damiDTO.email)
    }

    @Test
    fun testConvertActivationToDTO() {
        val dami = LANDUser("ozerodb", "lmao", "ozerodb@outlook.com")
        //given 7 days to activate so that the server running doesn't prune the activation while testing
        val expiringDate = Date(Date().time + 7 * 24 * 60 * 60 * 1000)
        val activation = LANDActivation(dami, "123", expiringDate)
        val activationDTO = activation.toActivationDTO()
        assertThat(activationDTO.id).isEqualTo(activation.id)
        assertThat(activationDTO.user).isEqualTo(dami)
        assertThat(activationDTO.code).isEqualTo("123")
    }

    //Email service

    @Test
    fun testEmailInvalid() {
        assertThat(emailService.validateEmailAddress("ozerodb")).isFalse
    }

    @Test
    fun testEmailValid() {
        assertThat(emailService.validateEmailAddress("kotland.polito@gmail.com")).isTrue
    }

    @Test
    fun testSendEmailInvalid() {
        assertThrows(InvalidEmailAddressException::class.java) {
            emailService.sendEmail("invalidaddress", "Email unit test", "This is just a test")
        }
    }

    @Test
    fun testSendEmailValid() {
        assertDoesNotThrow {
            emailService.sendEmail("kotland.polito+unittest@gmail.com", "Email unit test", "This is just a test")
        }
    }



}