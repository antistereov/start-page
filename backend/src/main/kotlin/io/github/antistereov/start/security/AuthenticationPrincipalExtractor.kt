package io.github.antistereov.start.security

import io.github.antistereov.start.global.model.exception.InvalidPrincipalException
import io.github.antistereov.start.global.model.exception.MissingClaimException
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class AuthenticationPrincipalExtractor {

    private val logger = LoggerFactory.getLogger(AuthenticationPrincipalExtractor::class.java)

    fun getJwt(authentication: Authentication): Mono<Jwt> {
        logger.debug("Extracting JWT from authentication principal.")

        return Mono.justOrEmpty(authentication.principal)
            .cast(Jwt::class.java)
            .switchIfEmpty(Mono.error(InvalidPrincipalException("Invalid authentication principal.")))
    }

    fun getUserId(authentication: Authentication): Mono<String> {
        logger.debug("Extracting user ID from JWT.")

        return getJwt(authentication)
            .map { it.claims["sub"] as String }
            .switchIfEmpty(Mono.error(MissingClaimException("Missing 'sub' claim.")))
    }
}