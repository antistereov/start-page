package io.github.antistereov.start.auth.controller

import io.github.antistereov.start.auth.dto.LoginResponseDto
import io.github.antistereov.start.auth.service.AuthService
import io.github.antistereov.start.user.dto.LoginUserDto
import io.github.antistereov.start.user.dto.RegisterUserDto
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/login")
    suspend fun login(@RequestBody payload: LoginUserDto): LoginResponseDto {
        return authService.login(payload)
    }

    @PostMapping("/register")
    suspend fun register(@RequestBody payload: RegisterUserDto): LoginResponseDto {
        return authService.register(payload)
    }
}