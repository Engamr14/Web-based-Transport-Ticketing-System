package io.github.kotlandpolito.lab4traveler.security

import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy


@EnableWebSecurity
@Configuration
class WebSecurityConfig(val securityProperties: SecurityProperties) : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity) {
        /* configure URLs to protect */
        http
            .cors().and().csrf().disable()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // no sessions
            .and()
                .authorizeRequests().antMatchers("/ticket/validate").hasAnyAuthority("SYSTEM", "ADMIN")
                .antMatchers("/my/**").hasAuthority("CUSTOMER")
                .antMatchers("/admin/**").hasAuthority("ADMIN")
                .anyRequest().authenticated()
            .and()
                .addFilter(JWTAuthenticationFilter(authenticationManager(), securityProperties))
    }

}