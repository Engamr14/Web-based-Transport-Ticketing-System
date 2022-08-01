package io.github.kotlandpolito.landticketcatalogue.security

import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity

import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository

@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfiguration (val securityProperties: SecurityProperties) {

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .cors().disable()
            .csrf().disable()
            .httpBasic().disable()
            .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
            .addFilterAt(JWTAuthenticationFilter(securityProperties), SecurityWebFiltersOrder.AUTHORIZATION)
            .authorizeExchange().pathMatchers("/tickets").permitAll()
            .and()
            .authorizeExchange().pathMatchers("/shop/{ticket-id}", "/orders", "/orders/{order-id}").hasAuthority("CUSTOMER")
            .and()
            .authorizeExchange().pathMatchers("/admin/**").hasAuthority("ADMIN")
            .and()
            .build()
    }
}