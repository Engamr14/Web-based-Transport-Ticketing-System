package io.github.kotlandpolito.lab4traveler

import javax.persistence.*

@Entity
class Transit (
    /* User */
    @OneToOne var user: UserDetails,
    @OneToOne var ticket: TicketPurchased,
    @Column(name= "validation_time", nullable = false, updatable = false) var validation_time: Long = System.currentTimeMillis()
) {
    /* Unique Ticket ID (sub) */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    var id: Long? = null
    override fun toString() = "Transit {id:$id, user-id:${user.id}, ticket-id:${ticket.id}, validation-time:$validation_time}"
}

data class TransitDTO(
    val id: Long?,
    val userId: String?,
    val ticketId: Long?,
    val timestamp: Long
)

fun Transit.toTransitDTO() = TransitDTO(id,user.id,ticket.id,validation_time)
