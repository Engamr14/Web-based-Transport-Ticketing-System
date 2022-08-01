package io.github.kotlandpolito.lab3.user

import io.github.kotlandpolito.lab3.activation.ActivationDTO
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*

@Service
@Profile("controller-test")
class MockUserService : UserService {
    override fun startUserRegistration(username: String, password: String, email: String): ActivationDTO? {
        return ActivationDTO(
            UUID.randomUUID(),
            LANDUser(username, "password", "kotland.polito+mockuser@gmail.com"),
            attemptCounter = 0,
            code = "123456",
            Date()
        )
    }

    override fun verifyUserActivation(activationId: UUID, activationCode: String): UserDTO? {
        return UserDTO(1L, "username", "password", "kotland.polito+mockuser@gmail.com", Role.CUSTOMER, isActive = true)
    }

    override fun retrieveUser(userId: Long): UserDTO? {
        return UserDTO(1L, "username", "password", "kotland.polito+mockuser@gmail.com", Role.CUSTOMER, isActive = true)
    }

    override fun loginUser(userRequestDTO: UserRequestDTO): String? {
        return "1L,Date().time,Date().time+1000*60*60, arrayOf(Role.CUSTOMER)"
    }

    override fun enroll(adminUsername: String, user: BigUserRequestDTO): UserDTO? {
        TODO("Not yet implemented")
    }

}