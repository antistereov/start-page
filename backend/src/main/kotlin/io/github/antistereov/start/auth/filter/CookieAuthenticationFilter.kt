package io.github.antistereov.start.auth.filter

import io.github.antistereov.start.auth.exception.InvalidTokenException
import io.github.antistereov.start.auth.service.TokenService
import io.github.antistereov.start.user.service.UserService
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.mono
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class CookieAuthenticationFilter(
    private val tokenService: TokenService,
    private val userService: UserService,
) : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain) = mono {
        val authToken = extractTokenFromRequest(exchange)

        if (!authToken.isNullOrBlank()) {
            val userId = tokenService.getUserId(authToken)

            val user = userService.findByIdOrNull(userId)
                ?: throw InvalidTokenException("Access token belongs to user that does not exist")

            val roles = user.roles.map { SimpleGrantedAuthority("ROLE_$it") }

            val authentication = UsernamePasswordAuthenticationToken(
                userId, null, roles
            )

            val securityContext = SecurityContextImpl(authentication)
            return@mono chain.filter(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
                .awaitFirstOrNull()
        }

        chain.filter(exchange).awaitFirstOrNull()
    }

    /**
     * Extract token from request. The header has higher priority than the cookie.
     */
    private fun extractTokenFromRequest(exchange: ServerWebExchange): String? {
        return exchange.request.headers["Authorization"]
            ?.firstOrNull()?.removePrefix("Bearer ")
            ?: exchange.request.cookies["auth"]?.firstOrNull()?.value
    }
}