package io.github.antistereov.orbitab.user.service

import io.github.antistereov.orbitab.auth.exception.AuthException
import io.github.antistereov.orbitab.auth.exception.InvalidCredentialsException
import io.github.antistereov.orbitab.auth.exception.InvalidTokenException
import io.github.antistereov.orbitab.auth.properties.JwtProperties
import io.github.antistereov.orbitab.auth.service.AuthenticationService
import io.github.antistereov.orbitab.auth.service.HashService
import io.github.antistereov.orbitab.auth.service.TokenService
import io.github.antistereov.orbitab.config.properties.BackendProperties
import io.github.antistereov.orbitab.service.geolocation.GeoLocationService
import io.github.antistereov.orbitab.user.dto.LoginUserDto
import io.github.antistereov.orbitab.user.dto.RegisterUserDto
import io.github.antistereov.orbitab.user.exception.UsernameAlreadyExistsException
import io.github.antistereov.orbitab.user.dto.DeviceInfoRequestDto
import io.github.antistereov.orbitab.user.model.DeviceInfo
import io.github.antistereov.orbitab.user.model.UserDocument
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Service
import org.springframework.web.server.ServerWebExchange

@Service
class UserSessionService(
    private val userService: UserService,
    private val tokenService: TokenService,
    private val hashService: HashService,
    private val jwtProperties: JwtProperties,
    private val backendProperties: BackendProperties,
    private val authenticationService: AuthenticationService,
    private val geoLocationService: GeoLocationService,
) {

    private val logger: KLogger
        get() = KotlinLogging.logger {}

    suspend fun checkCredentialsAndGetUserId(payload: LoginUserDto): String {
        logger.debug { "Logging in user ${payload.username}" }
        val user = userService.findByUsername(payload.username)
            ?: throw InvalidCredentialsException()

        if (!hashService.checkBcrypt(payload.password, user.password)) {
            throw InvalidCredentialsException()
        }

        if (user.id == null) {
            throw AuthException("Login failed: UserDocument contains no id")
        }

        return user.id
    }

    suspend fun registerUserAndGetUserId(payload: RegisterUserDto): String {
        logger.debug { "Registering user ${payload.username}" }

        if (userService.existsByUsername(payload.username)) {
            throw UsernameAlreadyExistsException("Failed to register user ${payload.username}")
        }

        val userDocument = UserDocument(
            username = payload.username,
            password = hashService.hashBcrypt(payload.password)
        )

        val savedUserDocument = userService.save(userDocument)

        if (savedUserDocument.id == null) {
            throw AuthException("Login failed: UserDocument contains no id")
        }

        return savedUserDocument.id
    }

    suspend fun logout(deviceId: String): UserDocument {
        val userId = authenticationService.getCurrentUserId()
        val user = userService.findById(userId)
        val updatedDevices = user.devices.filterNot { it.deviceId == deviceId }

        return userService.save(user.copy(devices = updatedDevices))
    }

    fun createAccessTokenCookie(userId: String): ResponseCookie {
        val accessToken = tokenService.createAccessToken(userId)

        val cookie = ResponseCookie.from("access_token", accessToken)
            .httpOnly(true)
            .sameSite("Strict")
            .maxAge(jwtProperties.expiresIn)
            .path("/")

        if (backendProperties.secure) {
            cookie.secure(true)
        }
        return cookie.build()
    }

    suspend fun createRefreshTokenCookie(
        userId: String,
        deviceInfoDto: DeviceInfoRequestDto,
        ipAddress: String?
    ): ResponseCookie {
        val refreshToken = tokenService.createRefreshToken(userId, deviceInfoDto.deviceId)

        val location = ipAddress?.let { geoLocationService.getLocation(it) }

        val deviceInfo = DeviceInfo(
            deviceId = deviceInfoDto.deviceId,
            browser = deviceInfoDto.browser,
            os = deviceInfoDto.os,
            issuedAt = System.currentTimeMillis(),
            ipAddress = ipAddress,
            location = if (location != null) {
                DeviceInfo.LocationInfo(
                    location.latitude,
                    location.longitude,
                    location.cityName,
                    location.regionName,
                    location.countryCode
                )
            } else null,
        )

        userService.addOrUpdateDevice(userId, deviceInfo)

        val cookie = ResponseCookie.from("refresh_token", refreshToken)
            .httpOnly(true)
            .sameSite("Strict")
            .path("/auth/refresh")

        if (backendProperties.secure) {
            cookie.secure(true)
        }

        return cookie.build()
    }

    fun clearAccessTokenCookie(): ResponseCookie {
        val cookie = ResponseCookie.from("access_token", "")
            .httpOnly(true)
            .sameSite("Strict")
            .maxAge(0)
            .path("/")

        if (backendProperties.secure) {
            cookie.secure(true)
        }

        return cookie.build()

    }

    fun clearRefreshTokenCookie(): ResponseCookie {
        val cookie = ResponseCookie.from("refresh_token", "")
            .httpOnly(true)
            .sameSite("Strict")
            .maxAge(0)
            .path("/auth/refresh")

        if (backendProperties.secure) {
            cookie.secure(true)
        }

        return cookie.build()
    }

    suspend fun validateRefreshTokenAndGetUserId(exchange: ServerWebExchange, deviceInfoDto: DeviceInfoRequestDto): String {
        val refreshToken = exchange.request.cookies["refresh_token"]?.firstOrNull()?.value
            ?: throw InvalidTokenException("No refresh token provided")

        val userId = tokenService.validateRefreshTokenAndGetUserId(refreshToken, deviceInfoDto.deviceId)

        return userId
    }
}