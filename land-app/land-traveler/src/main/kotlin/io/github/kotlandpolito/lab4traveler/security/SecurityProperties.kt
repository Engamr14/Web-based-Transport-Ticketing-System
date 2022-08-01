package io.github.kotlandpolito.lab4traveler.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class SecurityProperties {

    @Value("\${server.jwt-key}")
    lateinit var jwtKey: String

    var tokenPrefix = "Bearer "
    var headerString = "Authorization"
}
