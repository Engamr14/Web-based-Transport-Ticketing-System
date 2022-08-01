package io.github.kotlandpolito.lab3.security

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
                .authorizeRequests()
                .antMatchers("/admin/enroll").hasAuthority("ADMIN")
                .anyRequest().permitAll()
            .and()
                .addFilter(JWTAuthenticationFilter(authenticationManager(), securityProperties))
    }

}