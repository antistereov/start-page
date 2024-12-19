package io.github.antistereov.orbitab.auth.service

import io.github.antistereov.orbitab.auth.exception.AccessTokenExpiredException
import io.github.antistereov.orbitab.auth.exception.InvalidTokenException
import io.github.antistereov.orbitab.auth.properties.JwtProperties
import io.github.antistereov.orbitab.user.service.UserService
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
    jwtProperties: JwtProperties,
    private val userService: UserService
) {

    private val tokenExpiresInSeconds = jwtProperties.expiresIn

    fun createAccessToken(userId: String): String {
        val jwsHeader = JwsHeader.with { "HS256" }.build()
        val claims = JwtClaimsSet.builder()
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(tokenExpiresInSeconds))
            .subject(userId)
            .build()

        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).tokenValue
    }

    suspend fun validateAccessTokenAndGetUserId(token: String): String {
        val jwt = jwtDecoder.decode(token).awaitFirstOrNull()
            ?: throw InvalidTokenException("Cannot decode access token")
        val expiresAt = jwt.expiresAt
            ?: throw InvalidTokenException("JWT does not contain expiration information")

        if (expiresAt <= Instant.now()) throw AccessTokenExpiredException("Access token is expired")

        return jwt.subject ?: throw InvalidTokenException("JWT does not contain sub")
    }

    fun createRefreshToken(userId: String, deviceId: String): String {
        val jwsHeader = JwsHeader.with { "HS256" }.build()
        val claims = JwtClaimsSet.builder()
            .subject(userId)
            .claim("device_id", deviceId)
            .issuedAt(Instant.now())
            .build()

        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).tokenValue
    }

    suspend fun validateRefreshTokenAndGetUserId(refreshToken: String, deviceId: String): String {
        val jwt = jwtDecoder.decode(refreshToken).awaitFirstOrNull()
            ?: throw InvalidTokenException("Cannot decode refresh token")

        val userId = jwt.subject ?: throw InvalidTokenException("Refresh token does not contain user id")

        val user = userService.findByIdOrNull(userId)
            ?: throw InvalidTokenException("No user exists with user id provided in refresh token")

        user.devices.firstOrNull { it.deviceId == deviceId }
            ?: throw InvalidTokenException("Device not authorized")

        return userId
    }
}