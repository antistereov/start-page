package io.github.antistereov.start.config

import io.github.antistereov.start.user.service.UserDetailsServiceImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class WebSecurityConfig {

    @Autowired
    private lateinit var passwordEncoder: BCryptPasswordEncoder

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http.invoke {
            csrf { disable() }
            authorizeRequests {
                authorize("/api/users/create", permitAll)
                authorize("/api/users/login", permitAll)
                authorize("/api/users/**", hasAuthority("ADMIN"))
                authorize(anyRequest, authenticated)
            }
            httpBasic {}
        }
        return http.build();
    }

    @Autowired
    private lateinit var userDetailsService: UserDetailsServiceImpl

    @Autowired
    fun configureGlobal(auth: AuthenticationManagerBuilder) {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder)
    }

}
