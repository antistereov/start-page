package io.github.antistereov.orbitab.connector.unsplash

import io.github.antistereov.orbitab.auth.service.AuthenticationService
import io.github.antistereov.orbitab.connector.unsplash.model.UnsplashPhoto
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/unsplash")
class UnsplashController(
    private val service: UnsplashService,
    private val authenticationService: AuthenticationService,
) {

    private val logger: KLogger
        get() = KotlinLogging.logger {}

    @GetMapping("/photo")
    suspend fun getRandomPhoto(
        @RequestParam screenWidth: Int,
        @RequestParam screenHeight: Int,
        @RequestParam quality: Int = 75,
    ): ResponseEntity<UnsplashPhoto> {
        logger.info { "Executing Unsplash getRandomPhoto" }

        val userId = authenticationService.getCurrentUserId()

        return ResponseEntity.ok(
            service.getRandomPhoto(userId, screenWidth, screenHeight, quality)
        )
    }

    @GetMapping("/photo/{id}")
    suspend fun getPhoto(
        @PathVariable id: String,
        @RequestParam screenWidth: Int,
        @RequestParam screenHeight: Int,
        @RequestParam quality: Int = 75,
    ): ResponseEntity<UnsplashPhoto> {
        logger.info { "Executing Unsplash getPhoto method with id: $id" }

        val userId = authenticationService.getCurrentUserId()

        return ResponseEntity.ok(
            service.getPhoto(userId, id, screenWidth, screenHeight, quality)
        )
    }

    @PostMapping("photo/{id}")
    suspend fun likePhoto(
        @PathVariable id: String
    ): ResponseEntity<Map<String, String>> {
        logger.info { "Executing Unsplash likePhoto method with id: $id" }

        val userId = authenticationService.getCurrentUserId()

        service.likePhoto(userId, id)

        return ResponseEntity.ok(
            mapOf("message" to "Photo successfully liked")
        )
    }

    @DeleteMapping("photo/{id}")
    suspend fun unlikePhoto(
        @PathVariable id: String
    ): ResponseEntity<Map<String, String>> {
        logger.info { "Executing Unsplash unlikePhoto method with id: $id" }

        val userId = authenticationService.getCurrentUserId()
        service.unlikePhoto(userId, id)

        return ResponseEntity.ok(
            mapOf("message" to "Photo successfully disliked")
        )
    }
}