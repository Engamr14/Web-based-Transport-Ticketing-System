package io.github.kotlandpolito.lab3.activation

import io.github.kotlandpolito.lab3.user.LANDUser
import org.hibernate.annotations.GenericGenerator
import java.util.*
import javax.persistence.*

@Entity
class LANDActivation(
    @OneToOne var user: LANDUser,
    @Column(name = "code", nullable = false) var code: String,
    @Temporal(TemporalType.TIMESTAMP) @Column(name = "expiration_date", nullable = false) var expirationDate: Date
) {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
        name = "UUID",
        strategy = "org.hibernate.id.UUIDGenerator",
    )
    @Column(name = "id", updatable = false, unique = true)
    var id: UUID? = null

    var attemptCounter: Int = 5
    override fun toString() = "Activation {$id, $user, $attemptCounter, $expirationDate}"
}

data class ActivationDTO(
    val id: UUID?, val user: LANDUser, val attemptCounter: Int, val code: String, val expirationDate: Date
)

data class ActivationRequestDTO(
    val provisional_id: String, val code: String
)

data class PendingActivationDTO(
    val provisional_id: String,
    val email: String,
)

fun LANDActivation.toActivationDTO() = ActivationDTO(id, user, attemptCounter, code, expirationDate)
fun ActivationDTO.toPendingActivationDTO() = PendingActivationDTO(provisional_id = id.toString(), email = user.email)