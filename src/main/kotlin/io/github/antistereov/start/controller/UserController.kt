package io.github.antistereov.start.controller

import io.github.antistereov.start.model.UserEntity
import io.github.antistereov.start.service.UserService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {
    @PostMapping
    fun createUser(@RequestBody user: UserEntity) = userService.createUser(user)
}