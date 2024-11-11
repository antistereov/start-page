package io.github.antistereov.start.auth.service

import io.github.antistereov.start.auth.exception.InvalidPrincipalException
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service

@Service
class PrincipalService {

    private val logger: KLogger
        get() = KotlinLogging.logger {}

    suspend fun getUserId(authentication: Authentication): String {
        logger.debug {"Extracting user ID from JWT." }

        return authentication.principal as? String
            ?: throw InvalidPrincipalException("Invalid authentication principal.")
    }
}