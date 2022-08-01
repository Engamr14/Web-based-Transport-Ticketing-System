package io.github.kotlandpolito.lab4traveler

import java.util.*
import javax.persistence.*

@Entity
class TicketPurchased(
    /* Zone ID */
    @Column(name = "zid", nullable = false) var zid: String,
    /* User */
    @ManyToOne var user: UserDetails
) {
    /* Unique Ticket ID (sub) */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    var id: Long? = null
    /* Issued At Timestamp */
    @Column(name = "iat", nullable = false) var iat: Long = System.currentTimeMillis()
    /* Expiry Timestamp */
    @Column(name = "exp", nullable = false) var exp: Long = iat + (60 * 60 * 1000) // + 1 hour
    /* JWS */
    @Column(name = "jws", nullable = true) var jws: String? = null
    override fun toString() = "Ticket {(sub)$id, iat:$iat, exp:$exp, zid:$zid, user:$user}"
}

data class TicketPurchasedDTO(
    val sub: Long?,
    val iat: Long,
    val exp: Long,
    val zid: String,
    val jws: String?
)

data class TicketPurchaseRequestDTO(
    val cmd: String,
    val quantity: Long,
    val zones: String
)

data class TicketPurchaseAdditionDTO(
    val quantity: Long,
    val validfrom: Long,
    val duration: Long,
    val zid: String,
    val type: String,
    val jws: String?
)

fun TicketPurchased.toTicketPurchasedDTO() = TicketPurchasedDTO(id, iat, exp, zid, jws)