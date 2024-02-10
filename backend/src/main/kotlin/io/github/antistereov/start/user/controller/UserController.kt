package io.github.antistereov.start.user.controller

import io.github.antistereov.start.user.service.UserService
import org.hibernate.service.spi.ServiceException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController {

    @Autowired
    private lateinit var userService: UserService

    @PostMapping("/token-login")
    fun handleLogin(authentication: Authentication): ResponseEntity<String> {
        val principal = authentication.principal as Jwt
        val userId = principal.claims["sub"].toString()

        return try {
            val user = userService.findOrCreateUser(userId)
            ResponseEntity.status(HttpStatus.OK).body(user.id)
        } catch (ex: ServiceException) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.message)
        }
    }

    @GetMapping("/profile")
    fun getUserProfile(authentication: Authentication): Map<String, Any> {
        val details = authentication.principal as Jwt
        return details.claims
    }
}