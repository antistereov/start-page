package io.github.antistereov.start.config

import io.github.antistereov.start.config.properties.Auth0Properties
import io.github.antistereov.start.security.AudienceValidator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.jwt.*

@Configuration
class JwtDecoderConfig(
    private val auth0Properties: Auth0Properties,
) {

    @Bean
    fun jwtDecoder(): JwtDecoder {
        val jwtDecoder = JwtDecoders.fromOidcIssuerLocation("https://${auth0Properties.domain}/") as NimbusJwtDecoder

        val audienceValidator: OAuth2TokenValidator<Jwt> = AudienceValidator(auth0Properties.audience)
        val withIssuer: OAuth2TokenValidator<Jwt> = JwtValidators.createDefaultWithIssuer("https://${auth0Properties.domain}/")
        val withAudience: OAuth2TokenValidator<Jwt> = DelegatingOAuth2TokenValidator(withIssuer, audienceValidator)

        jwtDecoder.setJwtValidator(withAudience)
        return jwtDecoder
    }
}