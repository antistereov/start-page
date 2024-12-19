package io.github.antistereov.orbitab.config

import io.github.antistereov.orbitab.auth.filter.CookieAuthenticationFilter
import io.github.antistereov.orbitab.auth.filter.LoggingFilter
import io.github.antistereov.orbitab.config.properties.FrontendProperties
import io.github.antistereov.orbitab.user.service.UserService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.ServerAuthenticationEntryPoint
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebFluxSecurity
@EnableMethodSecurity(prePostEnabled = true)
class WebSecurityConfig(
    private val cookieAuthenticationFilter: CookieAuthenticationFilter,
    private val loggingFilter: LoggingFilter,
    private val frontendProperties: FrontendProperties,
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun authenticationEntryPoint(): ServerAuthenticationEntryPoint {
        return HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)
    }

    @Bean
    fun filterChain(
        http: ServerHttpSecurity,
        userService: UserService,
    ): SecurityWebFilterChain {
        return http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .exceptionHandling {
                it.authenticationEntryPoint(authenticationEntryPoint())
            }
            .authorizeExchange {
                it.pathMatchers(
                    "/auth/spotify/callback",
                    "/auth/todoist/callback",
                    "/auth/unsplash/callback",
                    "/auth/instagram/callback",
                    "/auth/login",
                    "/auth/refresh",
                    "/auth/logout",
                    "/auth/check",
                ).permitAll()
                it.anyExchange().authenticated()
            }
            .addFilterBefore(loggingFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .addFilterBefore(cookieAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
            .build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = listOf(frontendProperties.baseUrl)
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("Authorization", "Content-Type")
        configuration.allowCredentials = true

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}
