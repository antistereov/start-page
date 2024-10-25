package io.github.antistereov.start.config

import io.github.antistereov.start.auth.service.TokenService
import io.github.antistereov.start.user.model.Role
import io.github.antistereov.start.user.service.UserService
import kotlinx.coroutines.runBlocking
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class WebSecurityConfig(
    private val tokenService: TokenService,
    private val userService: UserService,
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun filterChain(
        http: HttpSecurity,
        userService: UserService,
    ): SecurityFilterChain {
        http.invoke {
            csrf { disable() }
            authorizeRequests {
                authorize("/auth/spotify/callback", permitAll)
                authorize("/auth/todoist/callback", permitAll)
                authorize("/auth/unsplash/callback", permitAll)
                authorize("/auth/instagram/callback", permitAll)
                authorize("/api/auth/**", permitAll)
//                authorize("/api/users/login", permitAll)
//                authorize("/api/users/session-test", permitAll)
//                authorize("/api/users/**", hasRole("ADMIN"))
                authorize("/me", hasRole(Role.USER.toString()))
                authorize(anyRequest, authenticated)
            }
            oauth2ResourceServer {
                jwt {
                    jwtAuthenticationConverter = jwtAuthenticationConverter()
                }
            }
        }
        return http.build()
    }

    @Bean
    fun jwtAuthenticationConverter(): Converter<Jwt, AbstractAuthenticationToken> {
        return object : Converter<Jwt, AbstractAuthenticationToken> {

            override fun convert(jwt: Jwt): AbstractAuthenticationToken {
                val userId = tokenService.getUserId(jwt.tokenValue)
                    ?: throw InvalidBearerTokenException("Invalid token")
                val user = runBlocking { userService.findById(userId) }
                    ?: throw InvalidBearerTokenException("Invalid token")
                return UsernamePasswordAuthenticationToken(
                    userId,
                    "",
                    user.roles.map { SimpleGrantedAuthority("ROLE_$it") }
                )
            }
        }
    }
}
