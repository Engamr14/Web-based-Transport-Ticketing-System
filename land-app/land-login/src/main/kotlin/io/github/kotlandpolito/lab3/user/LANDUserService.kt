package io.github.kotlandpolito.lab3.user

//import org.springframework.scheduling.annotation.Scheduled

import io.github.kotlandpolito.lab3.activation.ActivationDTO
import io.github.kotlandpolito.lab3.activation.ActivationRepository
import io.github.kotlandpolito.lab3.activation.LANDActivation
import io.github.kotlandpolito.lab3.activation.toActivationDTO
import io.github.kotlandpolito.lab3.email.InvalidEmailAddressException
import io.github.kotlandpolito.lab3.email.LANDEmailService
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.hibernate.type.TrueFalseType
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.util.*
import java.util.regex.Pattern

@Service
@Profile("!controller-test")
class LANDUserService(val userRepo: UserRepository, val activationRepo: ActivationRepository) : UserService {

    //private final val usedSubs = ConcurrentHashMap<Int, String>().keySet("SET-ENTRY")
    @Value("\${server.jwt-key}")
    lateinit var jwtKey: String

    override fun startUserRegistration(username: String, password: String, email: String): ActivationDTO? {
        val activation: LANDActivation

        /* 1. Checks if the parameters passed are valid and not null or his unique values are in the db already */

        if (username.equals(null) || username == "") {
            throw InvalidUsernameException("No username passed")
        }
        if (password.equals(null) || password == "") {
            throw InvalidPasswordException("No password passed")
        }
        if (!validatePassword(password)) {
            throw InvalidPasswordException("This password is not valid: $password")
        }
        if (email.equals(null) || email == "") {
            throw InvalidEmailAddressException("No email passed")
        }
        if (!LANDEmailService().validateEmailAddress(email)) {
            throw InvalidEmailAddressException("This email is not valid: $email")
        }
        if (userRepo.findByUsername(username)?.equals(null) == false) {
            throw UsernameAlreadyUsedException("An user has already this username: $username")
        }
        if (userRepo.findByEmail(email)?.equals(null) == false) {
            throw EmailAddressAlreadyUsedException("An user has already this email: $email")
        }

        /* 2. Encrypts the password with a random salt and hashing function than, generates the user, constructor gives an id */

        val hash= BCryptPasswordEncoder(10, SecureRandom()).encode(password)
        val user = LANDUser(username, hash, email)

        /* 3. Generate activation code, expiration date then makes an instance of activation */

        //activation code generation

        val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        var activationCode = ""

        for (i in 0..32) {
            val random: Int = kotlin.random.Random.nextInt(0, charPool.size)
            val randomChar: Char = charPool[random]
            activationCode += randomChar
        }

        //expiration date generation

        val actualDateInMilliseconds: Long = Date().time

        //given a number of days in which the user can validate calculates the number of milliseconds
        val days: Long = 7
        val hours: Long = days * 24
        val minutes: Long = hours * 60
        val seconds: Long = minutes * 60
        val expiringTimeInMilliseconds: Long = seconds * 1000
        val expiringDate = Date(actualDateInMilliseconds + expiringTimeInMilliseconds)

        activation = LANDActivation(user, activationCode, expiringDate)

        /* 4. Sends an email to the user with the activationCode and the expiringDate */

        LANDEmailService().sendEmail(
            email,
            "Registration to LAND",
            "Welcome to the LAND, you have time to activate your account till $expiringDate using the code: $activationCode"
        )

        /* 5. Saves the user and the validation in the db and returns the activation DTO */

        userRepo.save(user)
        activationRepo.save(activation)
        return activation.toActivationDTO()
    }

    override fun verifyUserActivation(activationId: UUID, activationCode: String): UserDTO? {

        /* 1. Gets the activation from id, if that one has the UUID given to the function proceed otherwise throws NoSuchElementException */

        val optActivation: Optional<LANDActivation> = activationRepo.findById(activationId)
        if (!optActivation.isPresent) return null

        val activation = optActivation.get()
        val userToActive: LANDUser? = activation.user.let {
            it.id?.let { it1 ->
                userRepo.findById(it1).get()
            }
        }

        /* 1.5. Check that the activation request has been received in time, else remove it */
        if (activation.expirationDate.before(Date())){
            userToActive?.let {
                activationRepo.delete(activation)
                userRepo.delete(it)
            }
            return null
        }

        /*
            2. Retrieves from the activation the correspondent user and compares the activation code
            with the one brought by the controller.
                2.1. If the code is right updates the user isActive attribute and removes the activation from the activation table
                2.2. If the code is not right it checks how many attempts are remaining if equal to one it deletes the user and the activation
                otherwise it decrements the attempts in the activation tuple in the db.
        */

        var returnValue: Long? = null

        if (activation.code == activationCode) {
            userToActive?.let {
                activationRepo.delete(activation)
                it.isActive = true
                userRepo.save(it)
                returnValue = it.id
            }
        } else {
            if (activation.attemptCounter == 1) {
                userToActive?.let {
                    activationRepo.delete(activation)
                    userRepo.delete(it)
                }
            } else if (activation.attemptCounter > 1) {
                activation.attemptCounter--
                activationRepo.save(activation)
            }
        }

        /* 3. Returns the userDTO if activated and null if not */

        return returnValue?.let { userRepo.findbyId(it)?.toUserDTO() }
    }

    override fun retrieveUser(userId: Long): UserDTO? = userId.let { userRepo.findbyId(it)?.toUserDTO() }

    @Scheduled(fixedDelay = 200000)
    fun pruneActivation() {
        /*
        1. Fetch all activation with expired date, for each one fetch the user connected with it
        2. Delete both the activation and user
         */
        activationRepo.findAll().filter {
            it.expirationDate > Date()
        }.forEach {
            val user: LANDUser = it.user
            userRepo.delete(user)
            activationRepo.delete(it)
        }
    }

    private fun validatePassword(password: String): Boolean {
        val passwordREGEX = Pattern.compile(
            "^" + "(?=.*\\d)" +         //at least 1 digit
                    "(?=.*[a-z])" +         //at least 1 lower case letter
                    "(?=.*[A-Z])" +         //at least 1 upper case letter
                    "(?=.*[a-zA-Z])" +      //any letter
                    "(?=.*[!.@#$%^&+=])" +    //at least 1 special character
                    "(?=\\S+$)" +           //no white spaces
                    ".{8,}" +               //at least 8 characters
                    "$"
        )
        return passwordREGEX.matcher(password).matches()
    }

    override fun loginUser(userRequestDTO: UserRequestDTO): String? {
        val username: String=userRequestDTO.username
        val password: String=userRequestDTO.password
        val user: LANDUser = userRepo.findByUsername(username)
            ?: throw UsernameDoesntExistException("The username is not present in the database")
        val iat: Long
        val exp: Long

        user.let {

            if (!BCryptPasswordEncoder(10, SecureRandom()).matches(password, user.hash)) {
                throw PasswordDoesntMatchException("Incorrect password for this user")
            }

            iat = System.currentTimeMillis()
            exp = iat + 1000 * 60 * 60

            val key = Keys.hmacShaKeyFor(jwtKey.toByteArray())
            val jwt = Jwts.builder()
                .setHeaderParam("alg", "HS256")
            jwt
                //.setSubject(sub.toString())
                .setSubject(user.username)
                .setIssuedAt(Date(iat))
                .setExpiration(Date(exp))
                .claim("role", user.role.toString())
                .claim("user-id", user.id.toString())


            return jwt.signWith(key).compact()

        }

    }

    override fun enroll(adminUsername: String, user: BigUserRequestDTO): UserDTO? {
        val admin: LANDUser = userRepo.findByUsername(adminUsername)
            ?: throw UsernameDoesntExistException("The admin username is not present in the database")
        if (!admin.canEnroll){
            throw AdminCannotEnrollException("The admin cannot enroll new users")
        }
        /* 1. Checks if the parameters passed are valid and not null or his unique values are in the db already */
        val username = user.username
        val password = user.password
        val email = user.email

        if (username.equals(null) || username == "") {
            throw InvalidUsernameException("No username passed")
        }
        if (password.equals(null) || password == "") {
            throw InvalidPasswordException("No password passed")
        }
        if (!validatePassword(password)) {
            throw InvalidPasswordException("This password is not valid: $password")
        }
        if (email.equals(null) || email == "") {
            throw InvalidEmailAddressException("No email passed")
        }
        if (!LANDEmailService().validateEmailAddress(email)) {
            throw InvalidEmailAddressException("This email is not valid: $email")
        }
        if (userRepo.findByUsername(username)?.equals(null) == false) {
            throw UsernameAlreadyUsedException("An user has already this username: $username")
        }
        if (userRepo.findByEmail(email)?.equals(null) == false) {
            throw EmailAddressAlreadyUsedException("An user has already this email: $email")
        }

        val hash= BCryptPasswordEncoder(10, SecureRandom()).encode(password)
        val newUser = LANDUser(username, hash, email)

        newUser.role = Role.valueOf(user.role)
        newUser.isActive = true
        newUser.canEnroll = user.canEnroll.toBoolean()
        userRepo.save(newUser)

        return userRepo.findByUsername(user.username)!!.toUserDTO()
    }

}