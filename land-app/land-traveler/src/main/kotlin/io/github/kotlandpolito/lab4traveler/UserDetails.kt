package io.github.kotlandpolito.lab4traveler

import io.github.kotlandpolito.lab4traveler.TicketPurchased
import javax.persistence.*

@Entity
class UserDetails (
    @Id
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    var id: String,
    @Column(name = "name", nullable = false, unique = true) var name: String,
    @Column(name = "address", nullable = false) var address: String,
    @Column(name = "date_of_birth", nullable = false) var date_of_birth: String,
    @Column(name = "telephone_number", nullable = false, unique = true) var telephone_number: Long
    ){
    @OneToMany(mappedBy = "user")
    var tickets: List<TicketPurchased> = emptyList()

    override fun toString() = "User {$id, $name, $address, $date_of_birth, $telephone_number, $tickets}"
}

data class UserDetailsDTO(
    val id: String,
    val name: String,
    val address: String?,
    val date_of_birth: String?,
    val telephone_number: Long?,
    val role: String?
)

data class UserDetailsUpdateRequestDTO(
    val address: String,
    val date_of_birth: String,
    val telephone_number: Long
)

fun UserDetails.toUserDetailsDTO() = UserDetailsDTO(id, name, address, date_of_birth, telephone_number, role = null)