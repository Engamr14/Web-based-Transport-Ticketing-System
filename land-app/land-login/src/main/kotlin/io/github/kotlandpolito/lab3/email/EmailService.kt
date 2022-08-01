package io.github.kotlandpolito.lab3.email

interface EmailService {

    /**
     * Send an email to the specified address.
     *
     * This function takes the recipient's email address, the email's subject and body,
     * and sends an email.
     *
     * @param address the email address of the recipient.
     * @param subject the subject of the email.
     * @param body the content of the email.
     * @return whether the email was sent or not.
     * @throws InvalidEmailAddressException if the email address is invalid
     */
    fun sendEmail(address: String, subject: String, body: String): Boolean

    /**
     * Check that an email address is valid
     *
     * @param address the email address to be checked.
     * @return whether the email address is valid or not.
     */
    fun validateEmailAddress(address: String): Boolean
}

class InvalidEmailAddressException(message: String) : Exception(message)


