package io.github.antistereov.start.user.controller

import io.github.antistereov.start.auth.service.AuthenticationService
import io.github.antistereov.start.user.model.UserDocument
import io.github.antistereov.start.user.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping
class UserController(
    private val userService: UserService,
    private val principalExtractor: AuthenticationService,
) {

    @GetMapping("/me")
    suspend fun getUserProfile(): ResponseEntity<UserDocument?> {
        val userId = principalExtractor.getCurrentUserId()

        // TODO: Catch null case
        return ResponseEntity.ok(
            userService.findById(userId)
        )
    }

    @DeleteMapping("/me")
    suspend fun deleteUser(): ResponseEntity<Any> {
        val userId = principalExtractor.getCurrentUserId()
        return ResponseEntity.ok(
            userService.delete(userId)
        )
    }
}
