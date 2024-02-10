package io.github.antistereov.start.config

import io.github.antistereov.start.security.AudienceValidator
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.jwt.*
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.reactive.function.client.WebClient
import java.util.*

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class WebSecurityConfig {

    @Value("\${auth0.domain}")
    private lateinit var domain: String

    @Value("\${auth0.audience}")
    private lateinit var audience: String

    @Value("\${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private lateinit var issuer: String

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http.invoke {
            csrf { disable() }
            oauth2ResourceServer {
                jwt {
                    jwkSetUri = "https://$domain/.well-known/jwks.json"
                    jwtAuthenticationConverter = jwtAuthenticationConverter()
                }
            }
            authorizeRequests {
                authorize("/api/spotify/callback", permitAll)
                authorize("/api/todoist/callback", permitAll)
//                authorize("/api/users/login", permitAll)
//                authorize("/api/users/session-test", permitAll)
//                authorize("/api/users/**", hasRole("ADMIN"))
                authorize(anyRequest, authenticated)
            }
        }
        return http.build();
    }

    @Bean
    fun jwtDecoder(): JwtDecoder {
        val jwtDecoder = JwtDecoders.fromOidcIssuerLocation(issuer) as NimbusJwtDecoder

        val audienceValidator: OAuth2TokenValidator<Jwt> = AudienceValidator(audience)
        val withIssuer: OAuth2TokenValidator<Jwt> = JwtValidators.createDefaultWithIssuer(issuer)
        val withAudience: OAuth2TokenValidator<Jwt> = DelegatingOAuth2TokenValidator(withIssuer, audienceValidator)

        val authoritiesConverter = GrantedAuthoritiesClaimTokenConverter()
        val claimTypeConverter: MutableMap<String, Converter<Any, *>> = mutableMapOf("authorities" to authoritiesConverter)

        val delegatingConverter = MappedJwtClaimSetConverter.withDefaults(claimTypeConverter)

        jwtDecoder.setClaimSetConverter(delegatingConverter)
        jwtDecoder.setJwtValidator(withAudience)
        return jwtDecoder
    }

    class GrantedAuthoritiesClaimTokenConverter : Converter<Any, Collection<GrantedAuthority>> {
        override fun convert(roles: Any): Collection<GrantedAuthority> {
            val list = roles as List<*>
            return list.map { SimpleGrantedAuthority("ROLE_${it.toString().uppercase(Locale.getDefault())}") }
        }
    }

    @Bean
    fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
        val jwtGrantedAuthoritiesConverter = JwtGrantedAuthoritiesConverter()
        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("authorities")
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("")

        val jwtAuthenticationConverter = JwtAuthenticationConverter()
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter)
        return jwtAuthenticationConverter
    }

    @Bean
    fun webClientBuilder(): WebClient.Builder {
        return WebClient.builder()
    }

    @Bean
    fun webClient(): WebClient {
        return WebClient.create()
    }

}
