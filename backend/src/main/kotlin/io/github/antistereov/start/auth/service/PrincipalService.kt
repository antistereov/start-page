package io.github.antistereov.start.auth.service

import io.github.antistereov.start.global.exception.InvalidPrincipalException
import io.github.antistereov.start.auth.exception.MissingClaimException
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service

@Service
class PrincipalService {

    private val logger: KLogger
        get() = KotlinLogging.logger {}

    private suspend fun getJwt(authentication: Authentication): Jwt {
        logger.debug { "Extracting JWT from authentication principal." }

        return authentication.principal as? Jwt
            ?: throw InvalidPrincipalException("Invalid authentication principal.")
    }

    suspend fun getUserId(authentication: Authentication): String {
        logger.debug {"Extracting user ID from JWT." }

        val principal = getJwt(authentication)
        return try {
            principal.claims["sub"] as String
        } catch (e: NoSuchElementException) {
            throw MissingClaimException("Missing 'sub' claim.")
        }
    }
}