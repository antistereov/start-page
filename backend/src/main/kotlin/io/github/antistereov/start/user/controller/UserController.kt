package io.github.antistereov.start.user.controller

import io.github.antistereov.start.security.AuthenticationPrincipalExtractor
import io.github.antistereov.start.user.model.User
import io.github.antistereov.start.user.service.UserService
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping
class UserController(
    private val userService: UserService,
    private val principalExtractor: AuthenticationPrincipalExtractor,
) {

    @PostMapping("/auth")
    fun handleAuth(authentication: Authentication): Mono<User> {
        return principalExtractor.getUserId(authentication)
            .flatMap { userService.findOrCreateUser(it) }
    }

    @GetMapping("/me")
    fun getUserProfile(authentication: Authentication): Mono<Map<String, Any>> {
        return principalExtractor.getJwt(authentication)
            .map { it.claims }
    }
}