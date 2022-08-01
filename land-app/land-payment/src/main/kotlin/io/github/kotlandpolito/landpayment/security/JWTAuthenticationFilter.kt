package io.github.kotlandpolito.landpayment.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.http.HttpHeaders
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.util.StringUtils
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

class JWTAuthenticationFilter(private val securityProperties: SecurityProperties) : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val token = resolveToken(exchange.request)
        if (StringUtils.hasText(token) && validateToken(token)) {
            val authentication: Authentication = getAuthentication(token)
            return chain.filter(exchange).contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication))
        }
        return chain.filter(exchange)
    }

    private fun resolveToken(request: ServerHttpRequest): String {
        val bearerToken: String? = request.headers.getFirst(securityProperties.headerString)
        if (bearerToken != null) {
            if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(securityProperties.tokenPrefix)) {
                return bearerToken.substring(securityProperties.tokenPrefix.length)
            }
        }
        return ""
    }

    private fun validateToken(token: String): Boolean {
        return try {
            val claims: Jws<Claims> = Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(securityProperties.jwtKey.toByteArray())).build().parseClaimsJws(token)
            //  parseClaimsJws will check expiration date. No need do here.
            true
        } catch (e: JwtException) {
            false
        }
    }

    private fun getAuthentication(token: String): UsernamePasswordAuthenticationToken {
        val claims: Claims = Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(securityProperties.jwtKey.toByteArray())).build().parseClaimsJws(token).body

        //val username = claims["sub"] as String
        val username = claims["user-id"] as String
        val userRole = claims["role"] as String

        val authorities = ArrayList<GrantedAuthority>()
        authorities.add(SimpleGrantedAuthority("CUSTOMER"))
        if (userRole == "ADMIN"){
            authorities.add(SimpleGrantedAuthority("ADMIN"))
        }
        val principal = User(username, "", authorities)
        return UsernamePasswordAuthenticationToken(principal, token, authorities)
    }

}
