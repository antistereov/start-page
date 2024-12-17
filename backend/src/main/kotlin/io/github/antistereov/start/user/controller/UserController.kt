package io.github.antistereov.start.user.controller

import io.github.antistereov.start.auth.service.PrincipalService
import io.github.antistereov.start.user.model.UserDocument
import io.github.antistereov.start.user.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping
class UserController(
    private val userService: UserService,
    private val principalExtractor: PrincipalService,
) {

    @GetMapping("/me")
    suspend fun getUserProfile(authentication: Authentication): ResponseEntity<UserDocument?> {
        val userId = principalExtractor.getUserId(authentication)

        // TODO: Catch null case
        return ResponseEntity.ok(
            userService.findById(userId)
        )
    }

    @DeleteMapping("/me")
    suspend fun deleteUser(authentication: Authentication): ResponseEntity<Any> {
        val userId = principalExtractor.getUserId(authentication)
        return ResponseEntity.ok(
            userService.delete(userId)
        )
    }
}
