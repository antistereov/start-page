package io.github.antistereov.start.spotify.controller

import io.github.antistereov.start.spotify.service.SpotifyService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/spotify")
class SpotifyController{

    @Autowired
    private lateinit var spotifyService: SpotifyService

    @GetMapping("/login")
    fun login(authentication: Authentication): String {
        val principal = authentication.principal as Jwt
        val userId = principal.claims["sub"].toString()

        return "redirect:${spotifyService.getAuthorizationUrl(userId)}"
    }

    @GetMapping("/callback")
    fun callback(@RequestParam code: String, @RequestParam state: String): ResponseEntity<String> {
        return try {
            spotifyService.authenticate(code, state)
            ResponseEntity.ok("Authentication successful.")
        } catch (e: RuntimeException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Authentication failed: $e")
        }
    }

    @GetMapping("/current-song")
    fun getCurrentSong(authentication: Authentication): Mono<String> {
        val principal = authentication.principal as Jwt
        val userId = principal.claims["sub"].toString()

        val accessToken = spotifyService.getAccessToken(userId)

        return spotifyService.getCurrentSong(accessToken)
    }

}