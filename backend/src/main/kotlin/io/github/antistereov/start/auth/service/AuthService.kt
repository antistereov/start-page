package io.github.antistereov.start.auth.service

import io.github.antistereov.start.auth.model.SessionCookieData
import io.github.antistereov.start.auth.exception.AuthException
import io.github.antistereov.start.auth.exception.InvalidCredentialsException
import io.github.antistereov.start.auth.properties.JwtProperties
import io.github.antistereov.start.user.dto.LoginUserDto
import io.github.antistereov.start.user.dto.RegisterUserDto
import io.github.antistereov.start.user.exception.UsernameAlreadyExistsException
import io.github.antistereov.start.user.model.UserDocument
import io.github.antistereov.start.user.service.UserService
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userService: UserService,
    private val tokenService: TokenService,
    private val hashService: HashService,
    private val jwtProperties: JwtProperties,
) {

    private val logger: KLogger
        get() = KotlinLogging.logger {}

    suspend fun login(payload: LoginUserDto): SessionCookieData {
        logger.debug { "Logging in user ${payload.username}" }
        val user = userService.findByUsername(payload.username)
            ?: throw InvalidCredentialsException()

        if (!hashService.checkBcrypt(payload.password, user.password)) {
            throw InvalidCredentialsException()
        }

        if (user.id == null) {
            throw AuthException("Login failed: UserDocument contains no id")
        }

        logger.debug { "Successfully logged in user ${payload.username}" }

        return SessionCookieData(
            accessToken = tokenService.createToken(user.id),
            expiresIn = jwtProperties.expiresIn,
        )
    }

    suspend fun register(payload: RegisterUserDto): SessionCookieData {
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

        logger.debug { "Successfully registered user ${payload.username}" }

        return SessionCookieData(
            accessToken = tokenService.createToken(savedUserDocument.id),
            expiresIn = jwtProperties.expiresIn,
        )
    }
}