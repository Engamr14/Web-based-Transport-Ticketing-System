package io.github.kotlandpolito.landticketcatalogue.model

data class UserDetails(
    val name: String,
    val address: String?,
    val date_of_birth: String?,
    val telephone_number: Long?,
    val role: String?
)
