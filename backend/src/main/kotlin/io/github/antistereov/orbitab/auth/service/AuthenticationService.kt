package io.github.antistereov.orbitab.auth.service

import io.github.antistereov.orbitab.auth.exception.InvalidPrincipalException
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContext
import org.springframework.stereotype.Service

@Service
class AuthenticationService {

    private val logger: KLogger
        get() = KotlinLogging.logger {}

    suspend fun getCurrentUserId(): String {
        logger.debug {"Extracting user ID." }

        val auth = getCurrentAuthentication()
        return auth.name ?: throw InvalidPrincipalException("Missing or invalid authentication principal.")
    }

    private suspend fun getCurrentAuthentication(): Authentication {
        val securityContext: SecurityContext = ReactiveSecurityContextHolder.getContext().awaitFirstOrNull()
            ?: throw InvalidPrincipalException("No security context found.")

        return securityContext.authentication
            ?: throw InvalidPrincipalException("Authentication is missing.")

    }
}