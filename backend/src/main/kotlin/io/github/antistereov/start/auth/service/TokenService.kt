package io.github.antistereov.start.auth.service

import io.github.antistereov.start.auth.properties.JwtProperties
import io.github.antistereov.start.user.service.UserService
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class TokenService(
    private val jwtDecoder: JwtDecoder,
    private val jwtEncoder: JwtEncoder,
    jwtProperties: JwtProperties
) {

    private val tokenExpiresInSeconds = jwtProperties.expiresInSeconds

    suspend fun createToken(userId: String): String {
        val jwsHeader = JwsHeader.with { "HS256" }.build()
        val claims = JwtClaimsSet.builder()
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(tokenExpiresInSeconds))
            .subject(userId)
            .build()

        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).tokenValue
    }

    fun getUserId(token: String): String? {
        val jwt = jwtDecoder.decode(token)
        return try {
            jwt.claims["sub"] as String
        } catch (e: NoSuchElementException) {
            null
        }
    }
}