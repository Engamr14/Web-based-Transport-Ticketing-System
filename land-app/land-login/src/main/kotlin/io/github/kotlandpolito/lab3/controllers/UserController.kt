package io.github.kotlandpolito.lab3.controllers

import io.github.kotlandpolito.lab3.activation.ActivationDTO
import io.github.kotlandpolito.lab3.activation.ActivationRequestDTO
import io.github.kotlandpolito.lab3.activation.PendingActivationDTO
import io.github.kotlandpolito.lab3.activation.toPendingActivationDTO
import io.github.kotlandpolito.lab3.user.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.*
import javax.servlet.http.HttpServletResponse

@RestController
class UserController {

    @Autowired
    lateinit var userService: UserService

    @PostMapping("/user/register")
    fun registerUser(@RequestBody user: UserRequestDTO, response: HttpServletResponse): PendingActivationDTO? {
        try {
            val activationDTO: ActivationDTO? =
                userService.startUserRegistration(user.username, user.password, user.email)

            response.status = HttpStatus.ACCEPTED.value()
            return activationDTO?.toPendingActivationDTO()
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
        }
    }

    @PostMapping("/user/verify")
    fun verifyUser(@RequestBody activation: ActivationRequestDTO, response: HttpServletResponse): VerifiedUserDTO? {

        val userId =
            userService.verifyUserActivation(UUID.fromString(activation.provisional_id), activation.code)?.id
                ?: throw ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Provisional ID verification failed"
                )

        val userDTO =
            userService.retrieveUser(userId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")

        response.status = HttpStatus.CREATED.value()
        return userDTO.toVerifiedUserDTO()
    }

    @PostMapping("/login")
    fun loginUser(@RequestBody userRequestDTO: UserRequestDTO, response: HttpServletResponse): String? {
        try {
            val logInfo: String? = userService.loginUser(userRequestDTO)
            if (logInfo==null){
                response.status= HttpStatus.FORBIDDEN.value()
            }else{
                response.status= HttpStatus.OK.value()
            }
            return logInfo
        }catch (e: PasswordDoesntMatchException){
            response.status= HttpStatus.FORBIDDEN.value()
            return null
        }
    }

    @PostMapping("/admin/enroll")
    fun enroll(@RequestBody user: BigUserRequestDTO, response: HttpServletResponse): VerifiedUserDTO? {
        try {
            val adminUsername = SecurityContextHolder.getContext().authentication.name

            val userDTO =
                userService.enroll(adminUsername, user) ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "User could not be enrolled")

            response.status = HttpStatus.CREATED.value()
            return userDTO.toVerifiedUserDTO()
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
        }

    }

}
