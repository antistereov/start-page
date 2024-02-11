package io.github.antistereov.start.security

import io.github.antistereov.start.model.InvalidPrincipalException
import io.github.antistereov.start.model.MissingClaimException
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class AuthenticationPrincipalExtractor {

    fun getJwt(authentication: Authentication): Mono<Jwt> {
        return Mono.justOrEmpty(authentication.principal)
            .cast(Jwt::class.java)
            .switchIfEmpty(Mono.error(InvalidPrincipalException("Invalid authentication principal.")))
    }

    fun getUserId(authentication: Authentication): Mono<String> {
        return getJwt(authentication)
            .map { it.claims["sub"] as String }
            .switchIfEmpty(Mono.error(MissingClaimException("Missing 'sub' claim.")))
    }
}