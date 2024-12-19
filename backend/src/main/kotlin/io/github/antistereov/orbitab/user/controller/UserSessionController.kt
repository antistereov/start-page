package io.github.antistereov.orbitab.user.controller

import io.github.antistereov.orbitab.user.service.UserSessionService
import io.github.antistereov.orbitab.auth.service.AuthenticationService
import io.github.antistereov.orbitab.user.dto.LoginUserDto
import io.github.antistereov.orbitab.user.dto.RegisterUserDto
import io.github.antistereov.orbitab.user.dto.DeviceInfoRequestDto
import io.github.antistereov.orbitab.user.model.DeviceInfo
import io.github.antistereov.orbitab.user.service.UserService
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ServerWebExchange

@RestController
@RequestMapping("/auth")
class UserSessionController(
    private val userSessionService: UserSessionService,
    private val authenticationService: AuthenticationService,
    private val userService: UserService,
) {

    private val logger: KLogger
        get() = KotlinLogging.logger {}

    @PostMapping("/login")
    suspend fun login(
        exchange: ServerWebExchange,
        @RequestBody payload: LoginUserDto
    ): ResponseEntity<Map<String, String>> {
        logger.info { "Executing login" }

        val userId = userSessionService.checkCredentialsAndGetUserId(payload)

        return generateTokensAndLogin(exchange, userId, payload.deviceInfoDto)
    }

    @PostMapping("/register")
    suspend fun register(
        exchange: ServerWebExchange,
        @RequestBody payload: RegisterUserDto
    ): ResponseEntity<Map<String, String>> {
        logger.info { "Executing register" }

        val userId = userSessionService.registerUserAndGetUserId(payload)

        return generateTokensAndLogin(exchange, userId, payload.deviceInfoDto)
    }

    private suspend fun generateTokensAndLogin(
        exchange: ServerWebExchange,
        userId: String,
        deviceInfo: DeviceInfoRequestDto,
    ): ResponseEntity<Map<String, String>> {
        val ipAddress = exchange.request.remoteAddress?.address?.hostAddress

        val accessTokenCookie = userSessionService.createAccessTokenCookie(userId)
        val refreshTokenCookie = userSessionService.createRefreshTokenCookie(userId, deviceInfo, ipAddress)

        return ResponseEntity.ok()
            .header("Set-Cookie", accessTokenCookie.toString())
            .header("Set-Cookie", refreshTokenCookie.toString())
            .body(mapOf(
                "user_id" to userId,
                "access_token" to accessTokenCookie.value,
                "refresh_token" to refreshTokenCookie.value
            ))
    }

    @PostMapping("/logout")
    suspend fun logout(@RequestParam deviceId: String): ResponseEntity<Map<String, String>> {
        logger.info { "Executing logout" }

        val clearAccessTokenCookie = userSessionService.clearAccessTokenCookie()
        val clearRefreshTokenCookie = userSessionService.clearRefreshTokenCookie()

        userSessionService.logout(deviceId)

        return ResponseEntity.ok()
            .header("Set-Cookie", clearAccessTokenCookie.toString())
            .header("Set-Cookie", clearRefreshTokenCookie.toString())
            .body(mapOf("message" to "Logout successful"))
    }

    @GetMapping("/check")
    suspend fun checkAuthentication(): ResponseEntity<Map<String, String>> {
        logger.info { "Checking authentication" }

        return try {
            val userId = authenticationService.getCurrentUserId()
            ResponseEntity.ok(mapOf("status" to "authenticated", "userId" to userId))
        } catch (ex: Exception) {
            ResponseEntity.status(401).body(mapOf("status" to "unauthenticated"))
        }
    }

    @PostMapping("/refresh")
    suspend fun refresh(
        exchange: ServerWebExchange,
        @RequestBody deviceInfoDto: DeviceInfoRequestDto
    ): ResponseEntity<Map<String, String>> {
        logger.info { "Refreshing access token" }

        val ipAddress = exchange.request.remoteAddress?.address?.hostAddress

        val userId = userSessionService.validateRefreshTokenAndGetUserId(exchange, deviceInfoDto)

        val newAccessToken = userSessionService.createAccessTokenCookie(userId)
        val newRefreshToken = userSessionService.createRefreshTokenCookie(userId, deviceInfoDto, ipAddress)

        return ResponseEntity.ok()
            .header("Set-Cookie", newAccessToken.toString())
            .header("Set-Cookie", newRefreshToken.toString())
            .body(mapOf("access_token" to newAccessToken.value, "refresh_token" to newRefreshToken.value))
    }

    @GetMapping("/devices")
    suspend fun getDevices(): ResponseEntity<Map<String, List<DeviceInfo>>> {
        val userId = authenticationService.getCurrentUserId()

        val devices = userService.getDevices(userId)

        return ResponseEntity.ok(
            mapOf("devices" to devices)
        )
    }

    @DeleteMapping("/devices")
    suspend fun deleteDevice(@RequestParam deviceId: String): ResponseEntity<Map<String, List<DeviceInfoRequestDto>>> {
        val userId = authenticationService.getCurrentUserId()

        val updatedUser = userService.deleteDevice(userId, deviceId)

        return ResponseEntity.ok(
            mapOf("devices" to updatedUser.devices.map { it.toDto() })
        )
    }
}