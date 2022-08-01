package io.github.kotlandpolito.lab4traveler.security

import io.github.kotlandpolito.lab4traveler.UserDetailsDTO
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import java.io.IOException
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JWTAuthenticationFilter(
    private val authManager: AuthenticationManager,
    private val securityProperties: SecurityProperties
    ): BasicAuthenticationFilter(authManager) {


    @Throws(IOException::class, ServletException::class)
    override fun doFilterInternal(
        req: HttpServletRequest,
        res: HttpServletResponse,
        chain: FilterChain
    ) {
        val header = req.getHeader(securityProperties.headerString)
        if (header == null || !header.startsWith(securityProperties.tokenPrefix)) {
            chain.doFilter(req, res)
            return
        }
        getAuthentication(header)?.also {
            SecurityContextHolder.getContext().authentication = it
        }
        chain.doFilter(req, res)
    }

    private fun getAuthentication(token_header: String): UsernamePasswordAuthenticationToken? {
        return try {
            val token = token_header.replace(securityProperties.tokenPrefix, "")
            if (!validateJwt(token)){
                return null
            }
            val currentUser : UserDetailsDTO = getDetailsJwt(token)

            val authorities = ArrayList<GrantedAuthority>()
            if (currentUser.role == "CUSTOMER" || currentUser.role == "ADMIN"){
                authorities.add(SimpleGrantedAuthority("CUSTOMER"))
            }
            if (currentUser.role == "ADMIN"){
                authorities.add(SimpleGrantedAuthority("ADMIN"))
            }
            if (currentUser.role == "SYSTEM"){
                authorities.add(SimpleGrantedAuthority("SYSTEM"))
            }

            UsernamePasswordAuthenticationToken(currentUser.name, null, authorities)
        } catch (e: Exception) {
            println(e)
            return null
        }
    }

    fun validateJwt(authToken: String): Boolean{
        val token = authToken.trim()

        if(token.isEmpty()){
            //println("Received empty token")
            return false
        }
        try {
            /* Try parsing the JWS, this includes checking its signature */
            Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(securityProperties.jwtKey.toByteArray())).build().parseClaimsJws(token)
        }
        catch (e : JwtException) {
            /* If the validation fails, then the JWS was most likely invalid */
            e.printStackTrace()
            return false
        }
        return true
    }

    fun getDetailsJwt(authToken: String): UserDetailsDTO {
        val token = authToken.trim()

        val claims: Claims = Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(securityProperties.jwtKey.toByteArray())).build().parseClaimsJws(token).body

        val id = claims["user-id"] as String
        val username = claims["sub"] as String
        val userRole = claims["role"] as String

        return UserDetailsDTO(id, username, null, null, null, userRole)
    }
}
