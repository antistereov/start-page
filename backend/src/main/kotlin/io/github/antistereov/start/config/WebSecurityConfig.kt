package io.github.antistereov.start.config

import io.github.antistereov.start.config.properties.Auth0Properties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class WebSecurityConfig(
    private val auth0Properties: Auth0Properties,
) {

    @Bean
    fun filterChain(
        http: HttpSecurity,
        converter: JwtAuthenticationConverter,
    ): SecurityFilterChain {
        http.invoke {
            csrf { disable() }
            oauth2ResourceServer {
                jwt {
                    jwkSetUri = "https://${auth0Properties.domain}/.well-known/jwks.json"
                    jwtAuthenticationConverter = converter
                }
            }
            authorizeRequests {
                authorize("/widgets/spotify/auth/callback", permitAll)
                authorize("/widgets/todoist/auth/callback", permitAll)
                authorize("/widgets/unsplash/auth/callback", permitAll)
                authorize("/widgets/instagram/auth/callback", permitAll)
//                authorize("/api/users/login", permitAll)
//                authorize("/api/users/session-test", permitAll)
//                authorize("/api/users/**", hasRole("ADMIN"))
                authorize(anyRequest, authenticated)
            }
        }
        return http.build()
    }
}
