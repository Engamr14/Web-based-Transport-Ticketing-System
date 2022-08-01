package io.github.kotlandpolito.lab3.email

import org.springframework.mail.MailException
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.stereotype.Service
import java.util.regex.Pattern

@Service
class LANDEmailService : EmailService {

    val mailSenderProperties = MailSenderProperties()
    private val sender: JavaMailSenderImpl = getJavaMailSender()

    override fun sendEmail(address: String, subject: String, body: String): Boolean {
        if (!validateEmailAddress(address)) {
            throw InvalidEmailAddressException("The email address is invalid: $address")
        }

        val mail = SimpleMailMessage()
        mail.setSubject(subject)
        mail.setTo(address)
        mail.setText(body)
        try {
            sender.send(mail)
        } catch (me: MailException) {
            // error while sending the email
            return false
        }
        return true
    }

    override fun validateEmailAddress(address: String): Boolean {
        return Pattern.compile("^[a-zA-Z\\d_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z\\d.-]+$").matcher(address).matches()
    }

    private final fun getJavaMailSender(): JavaMailSenderImpl {
        val mailSender = JavaMailSenderImpl()
        mailSender.host = mailSenderProperties.host
        mailSender.port = mailSenderProperties.port
        mailSender.username = mailSenderProperties.username
        mailSender.password = mailSenderProperties.password

        mailSender.javaMailProperties["mail.transport.protocol"] = mailSenderProperties.protocol
        mailSender.javaMailProperties["mail.smtp.auth"] = mailSenderProperties.auth
        mailSender.javaMailProperties["mail.smtp.starttls.enable"] = mailSenderProperties.starttlsEnable
        mailSender.javaMailProperties["mail.debug"] = mailSenderProperties.debug

        return mailSender
    }

}

class MailSenderProperties {
    val username: String = "kotland.polito@gmail.com"
    val password: String = "pleasedonthackus"

    val host: String = "smtp.gmail.com"
    val port: Int = 587

    val protocol: String = "smtp"
    val auth = "true"
    val starttlsEnable: String = "true"
    val debug: String = "false"
}
