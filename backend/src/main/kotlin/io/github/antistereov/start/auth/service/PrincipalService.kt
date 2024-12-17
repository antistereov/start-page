package io.github.antistereov.start.auth.service

import io.github.antistereov.start.auth.exception.InvalidPrincipalException
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.web.server.ServerWebExchange

@Service
class PrincipalService(
    val tokenService: TokenService
) {

    private val logger: KLogger
        get() = KotlinLogging.logger {}

    suspend fun getUserId(exchange: ServerWebExchange): String {
        logger.debug {"Extracting user ID from Cookie." }

        val token =  extractAuthToken(exchange)
        return tokenService.getUserId(token)
            ?: throw InvalidPrincipalException("Invalid authentication principal.")
    }

    private fun extractAuthToken(exchange: ServerWebExchange): String {
        val authToken = exchange.request.cookies["auth"]?.firstOrNull()?.value

        if (authToken == null || authToken == "") throw InvalidPrincipalException("Missing auth cookie.")

        return authToken
    }
}