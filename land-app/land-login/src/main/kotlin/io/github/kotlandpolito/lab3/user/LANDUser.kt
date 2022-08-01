package io.github.kotlandpolito.lab3.user

import javax.persistence.*

enum class Role {
    CUSTOMER,
    ADMIN,
    SYSTEM
}

@Entity
class LANDUser(
    @Column(name = "username", nullable = false, unique = true) var username: String,
    @Column(name = "hash", nullable = false) var hash: String,
    @Column(name = "email", nullable = false, unique = true) var email: String
) {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    var id: Long? = null

    // user's role implementation assuming their default role is CUSTOMER (ADMIN DOESN'T HAVE YET A REGISTRATION IN WITCH IS DECIDED HOW TO ASSIGN THIS ROLE)
    @Column(name="role", nullable = false, updatable = true, unique = false)
    var role: Role= Role.CUSTOMER

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = false

    @Column(name = "can_enroll", nullable = false)
    var canEnroll: Boolean = false
    override fun toString() = "User {$id, $username, $email, $role, $isActive}"
}

data class UserDTO(
    val id: Long?, val username: String, val password: String, val email: String, val role: Role, val isActive: Boolean
)

data class UserRequestDTO(
    val username: String,
    val password: String,
    val email: String,
)

data class BigUserRequestDTO(
    val username: String,
    val password: String,
    val email: String,
    val role: String,
    val canEnroll: String
)

data class VerifiedUserDTO(
    val id: Long?,
    val username: String,
    val email: String,
)

data class LoginJwtDTO(
    val sub: Long,
    val iat: Long,
    val exp: Long,
    val roles: Array<Role>
)
fun LANDUser.toUserDTO() = UserDTO(id, username, hash, email, role, isActive)
fun UserDTO.toVerifiedUserDTO() = VerifiedUserDTO(id, username, email)