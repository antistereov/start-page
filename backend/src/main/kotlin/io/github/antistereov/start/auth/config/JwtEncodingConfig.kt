package io.github.antistereov.start.auth.config

import com.nimbusds.jose.jwk.source.ImmutableSecret
import com.nimbusds.jose.proc.SecurityContext
import io.github.antistereov.start.auth.properties.JwtProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.jwt.*
import javax.crypto.spec.SecretKeySpec

@Configuration
class JwtEncodingConfig(
    jwtProperties: JwtProperties,
) {

    private val jwtKey = jwtProperties.secretKey
    private val secretKey = SecretKeySpec(jwtKey.toByteArray(), "HmacSHA256")

    @Bean
    fun reactiveJwtDecoder(): ReactiveJwtDecoder {
        return NimbusReactiveJwtDecoder.withSecretKey(secretKey).build()
    }

    @Bean
    fun jwtEncoder(): JwtEncoder {
        val secret = ImmutableSecret<SecurityContext>(secretKey)
        return NimbusJwtEncoder(secret)
    }
}