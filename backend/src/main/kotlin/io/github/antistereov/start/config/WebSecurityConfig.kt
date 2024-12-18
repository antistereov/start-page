package io.github.antistereov.start.config

import io.github.antistereov.start.auth.filter.CookieAuthenticationFilter
import io.github.antistereov.start.auth.filter.LoggingFilter
import io.github.antistereov.start.config.properties.FrontendProperties
import io.github.antistereov.start.user.service.UserService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
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
    fun filterChain(
        http: ServerHttpSecurity,
        userService: UserService,
    ): SecurityWebFilterChain {
        return http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
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
            .addFilterBefore(loggingFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .addFilterBefore(cookieAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = listOf(frontendProperties.baseUrl)  // Angular-Frontend
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("Authorization", "Content-Type")
        configuration.allowCredentials = true

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}
