package io.github.antistereov.start.user.controller

import io.github.antistereov.start.auth.service.PrincipalService
import io.github.antistereov.start.user.model.UserDocument
import io.github.antistereov.start.user.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ServerWebExchange

@RestController
@RequestMapping
class UserController(
    private val userService: UserService,
    private val principalExtractor: PrincipalService,
) {

    @GetMapping("/me")
    suspend fun getUserProfile(exchange: ServerWebExchange): ResponseEntity<UserDocument?> {
        val userId = principalExtractor.getUserId(exchange)

        // TODO: Catch null case
        return ResponseEntity.ok(
            userService.findById(userId)
        )
    }

    @DeleteMapping("/me")
    suspend fun deleteUser(exchange: ServerWebExchange): ResponseEntity<Any> {
        val userId = principalExtractor.getUserId(exchange)
        return ResponseEntity.ok(
            userService.delete(userId)
        )
    }
}
