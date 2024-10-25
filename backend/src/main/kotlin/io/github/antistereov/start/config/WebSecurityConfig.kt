package io.github.antistereov.start.config

import io.github.antistereov.start.auth.service.TokenService
import io.github.antistereov.start.user.service.UserService
import kotlinx.coroutines.runBlocking
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class WebSecurityConfig(
    private val tokenService: TokenService
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun filterChain(
        http: HttpSecurity,
        converter: JwtAuthenticationConverter, userService: UserService,
    ): SecurityFilterChain {
        http.invoke {
            csrf { disable() }
            authorizeRequests {
                authorize("/auth/spotify/callback", permitAll)
                authorize("/auth/todoist/callback", permitAll)
                authorize("/auth/unsplash/callback", permitAll)
                authorize("/auth/instagram/callback", permitAll)
                authorize("/api/auth", permitAll)
//                authorize("/api/users/login", permitAll)
//                authorize("/api/users/session-test", permitAll)
//                authorize("/api/users/**", hasRole("ADMIN"))
                authorize(anyRequest, authenticated)
            }
        }

        http.authenticationManager { auth ->
            val jwt = auth as BearerTokenAuthenticationToken
            val userId = tokenService.getUserId(jwt.token)
                ?: throw InvalidBearerTokenException("Invalid token")
            val user = runBlocking { userService.findById(userId) }
                ?: throw InvalidBearerTokenException("Invalid token")
            UsernamePasswordAuthenticationToken(
                userId,
                "",
                user.roles.map { SimpleGrantedAuthority(it.toString()) }
            )
        }
        return http.build()
    }
}
