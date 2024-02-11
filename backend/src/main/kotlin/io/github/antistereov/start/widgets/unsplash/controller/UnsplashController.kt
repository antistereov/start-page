package io.github.antistereov.start.widgets.unsplash.controller

import io.github.antistereov.start.widgets.unsplash.service.UnsplashService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/unsplash")
class UnsplashController(
    private val unsplashService: UnsplashService
) {

    @GetMapping("/login")
    fun login(authentication: Authentication): String {
        val principal = authentication.principal as Jwt
        val userId = principal.claims["sub"].toString()

        return "redirect:${unsplashService.getAuthorizationUrl(userId)}"
    }

    @GetMapping("/callback")
    fun callback(@RequestParam code: String, @RequestParam state: String): ResponseEntity<String> {
        return try {
            unsplashService.authenticate(code, state)
            ResponseEntity.ok("Authentication successful.")
        } catch (e: RuntimeException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Authentication failed: $e")
        }
    }

    @GetMapping("/photo")
    fun getRandomPhoto(@RequestParam query: String? = null): Mono<String> {
        return unsplashService.getRandomPhoto(query)
    }

    @GetMapping("/photo/{id}")
    fun getPhoto(@PathVariable id: String): Mono<String> {
        return unsplashService.getPhoto(id)
    }

    @PostMapping("photo/{id}/like")
    fun likePhoto(authentication: Authentication, @PathVariable id: String): Mono<String> {
        val principal = authentication.principal as Jwt
        val userId = principal.claims["sub"].toString()
        val accessToken = unsplashService.getAccessToken(userId)

        return unsplashService.likePhoto(accessToken, id)
    }

    @DeleteMapping("photo/{id}/like")
    fun unlikePhoto(authentication: Authentication, @PathVariable id: String): Mono<String> {
        val principal = authentication.principal as Jwt
        val userId = principal.claims["sub"].toString()
        val accessToken = unsplashService.getAccessToken(userId)

        return unsplashService.unlikePhoto(accessToken, id)
    }
}