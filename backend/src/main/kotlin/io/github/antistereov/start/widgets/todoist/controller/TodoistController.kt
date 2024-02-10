package io.github.antistereov.start.widgets.todoist.controller

import io.github.antistereov.start.widgets.todoist.service.TodoistService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/todoist")
class TodoistController {

    @Autowired
    private lateinit var todoistService: TodoistService

    @GetMapping("/login")
    fun login(authentication: Authentication): String {
        val principal = authentication.principal as Jwt
        val userId = principal.claims["sub"].toString()

        return "redirect:${todoistService.getAuthorizationUrl(userId)}"
    }

    @GetMapping("/callback")
    fun callback(@RequestParam code: String, @RequestParam state: String): ResponseEntity<String> {
        return try {
            todoistService.authenticate(code, state)
            ResponseEntity.ok("Authentication successful.")
        } catch (e: RuntimeException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Authentication failed: $e")
        }
    }

    @GetMapping("/tasks")
    fun getTasks(authentication: Authentication): Mono<String> {
        val principal = authentication.principal as Jwt
        val userId = principal.claims["sub"].toString()

        val accessToken = todoistService.getAccessToken(userId)

        return todoistService.getTasks(accessToken)
    }

}