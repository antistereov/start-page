package io.github.antistereov.start.auth.service

import io.github.antistereov.start.auth.exception.AccessTokenExpiredException
import io.github.antistereov.start.auth.exception.InvalidTokenException
import io.github.antistereov.start.auth.properties.JwtProperties
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class TokenService(
    private val jwtDecoder: ReactiveJwtDecoder,
    private val jwtEncoder: JwtEncoder,
    jwtProperties: JwtProperties
) {

    private val tokenExpiresInSeconds = jwtProperties.expiresIn

    fun createToken(userId: String): String {
        val jwsHeader = JwsHeader.with { "HS256" }.build()
        val claims = JwtClaimsSet.builder()
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(tokenExpiresInSeconds))
            .subject(userId)
            .build()

        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).tokenValue
    }

    suspend fun getUserId(token: String): String {
        val jwt = jwtDecoder.decode(token).awaitFirstOrNull()
            ?: throw InvalidTokenException("Cannot decode JWT")
        val expiresAt = jwt.expiresAt
            ?: throw InvalidTokenException("JWT does not contain expiration information")

        if (expiresAt <= Instant.now()) throw AccessTokenExpiredException("Access token is expired")

        return try {
            jwt.claims["sub"] as String
        } catch (e: NoSuchElementException) {
            throw InvalidTokenException("JWT does not contain sub")
        }
    }
}