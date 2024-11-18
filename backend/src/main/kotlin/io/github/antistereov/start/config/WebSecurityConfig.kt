package io.github.antistereov.start.config

import io.github.antistereov.start.auth.service.TokenService
import io.github.antistereov.start.user.model.Role
import io.github.antistereov.start.user.model.UserDocument
import io.github.antistereov.start.user.service.UserService
import kotlinx.coroutines.reactor.mono
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
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.invoke
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.server.SecurityWebFilterChain
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import kotlin.coroutines.suspendCoroutine

@Configuration
@EnableWebFluxSecurity
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
        http: ServerHttpSecurity,
        userService: UserService,
    ): SecurityWebFilterChain {
        return http
            .csrf { it.disable() }
            .authorizeExchange {
                it.pathMatchers(
                    "/auth/spotify/callback",
                    "/auth/todoist/callback",
                    "/auth/unsplash/callback",
                    "/auth/instagram/callback",
                    "/api/auth/**",
                ).permitAll()
                it.anyExchange().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                }
            }
            .build()
    }



    @Bean
    fun jwtAuthenticationConverter(): (Jwt) -> Mono<UsernamePasswordAuthenticationToken> {
        return { jwt ->
            mono { tokenService.getUserId(jwt.tokenValue) }
                .switchIfEmpty(Mono.error(InvalidBearerTokenException("Invalid token")))
                .flatMap { userId ->
                    mono { userService.findById(userId) }.map { user ->
                        UsernamePasswordAuthenticationToken(
                            userId,
                            "",
                            user.roles.map { SimpleGrantedAuthority("ROLE_$it") }
                        )
                    }
                }
                .onErrorResume { Mono.error(InvalidBearerTokenException("Invalid token")) }
        }
    }
}
