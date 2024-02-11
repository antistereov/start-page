package io.github.antistereov.start.user.controller

import io.github.antistereov.start.security.AuthenticationPrincipalExtractor
import io.github.antistereov.start.user.model.User
import io.github.antistereov.start.user.service.UserService
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService,
    private val authenticationPrincipalExtractor: AuthenticationPrincipalExtractor,
) {

    @PostMapping("/auth")
    fun handleAuth(authentication: Authentication): Mono<User> {
        return authenticationPrincipalExtractor.getUserId(authentication)
            .flatMap { userService.findOrCreateUser(it) }
    }

    @GetMapping("/profile")
    fun getUserProfile(authentication: Authentication): Mono<Map<String, Any>> {
        return authenticationPrincipalExtractor.getJwt(authentication)
            .map { it.claims }
    }
}