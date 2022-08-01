package io.github.kotlandpolito.lab3.user

import io.github.kotlandpolito.lab3.activation.ActivationDTO
import java.util.UUID

interface UserService {

    /**
     * Starts the user registration procedure
     *
     * This function takes as argument the username, password and email
     * of the new user, and starts a new registration procedure.
     *
     * @param username the username of the user.
     * @param password the password of the user.
     * @param email the email address of the user.
     * @return the ActivationDTO, if started correctly null if it doesn't.
     * @throws InvalidUsernameException if the username is empty.
     * @throws InvalidEmailAddressException if the email address is invalid or empty.
     * @throws UsernameAlreadyUsedException if the username is already in the database associated to an existent user.*
     * @throws EmailAddressAlreadyUsedException if the email address is in the database associated to an existent user.
     */
    fun startUserRegistration(username: String, password: String, email: String): ActivationDTO?

    /**
     * Verifies the user activation.
     *
     * This function takes as argument the UUID of the activation that
     * we would like to complete, and the associated activation code.
     *
     * @param activationId the UUID of the activation procedure.
     * @param activationCode the code associated to this specific activation.
     * @throws NoSuchElementException if the activation id doesn't match any record in the table
     * @return the UserDTO, if the activation was successful, null if not.
     */
    fun verifyUserActivation(activationId: UUID, activationCode: String): UserDTO?

    /**
     * Retrieves the user information
     *
     * This function takes as argument the id of a user, and retrieves
     * the information associated to that id, if any.
     *
     * @return the user information, if any
     */
    fun retrieveUser(userId: Long): UserDTO?

    /**
     *
     * Check if an user exist in the database then if the password matches returns information on the login.
     *
     * Takes as argument username and password.
     *
     * @return LoginJwtDTO(sub,iat,exp,roles).
     * @throws UsernameDoesntExistException if the username is not present in the database.
     * @throws PasswordDoesntMatchException if the password doesnt match with the hash saved in the db.
     */
    fun loginUser(userRequestDTO: UserRequestDTO): String?

    fun enroll(adminUsername: String, user: BigUserRequestDTO): UserDTO?
}

class InvalidUsernameException(message: String) : Exception(message)
class InvalidPasswordException(message: String) : Exception(message)
class UsernameAlreadyUsedException(message: String) : Exception(message)
class EmailAddressAlreadyUsedException(message: String) : Exception(message)
class InvalidEmailAddressException(message: String) : Exception(message)
class UsernameDoesntExistException(message: String) : Exception(message)
class PasswordDoesntMatchException(message: String) : Exception(message)
class AdminCannotEnrollException(message: String) : Exception(message)
