package io.github.antistereov.start.connector.unsplash

import io.github.antistereov.start.auth.service.PrincipalService
import io.github.antistereov.start.connector.unsplash.model.UnsplashPhoto
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/unsplash")
class UnsplashController(
    private val service: UnsplashService,
    private val principalService: PrincipalService,
) {

    private val logger: KLogger
        get() = KotlinLogging.logger {}

    @GetMapping("/photo")
    suspend fun getRandomPhoto(
        authentication: Authentication,
        @RequestParam screenWidth: Int,
        @RequestParam screenHeight: Int,
        @RequestParam quality: Int = 75,
    ): UnsplashPhoto {
        logger.info { "Executing Unsplash getRandomPhoto" }

        val userId = principalService.getUserId(authentication)

        return service.getRandomPhoto(userId, screenWidth, screenHeight, quality)
    }

    @GetMapping("/photo/{id}")
    suspend fun getPhoto(
        authentication: Authentication,
        @PathVariable id: String,
        @RequestParam screenWidth: Int,
        @RequestParam screenHeight: Int,
        @RequestParam quality: Int = 75,
    ): UnsplashPhoto {
        logger.info { "Executing Unsplash getPhoto method with id: $id" }

        val userId = principalService.getUserId(authentication)

        return service.getPhoto(userId, id, screenWidth, screenHeight, quality)
    }

    @PostMapping("photo/{id}")
    suspend fun likePhoto(authentication: Authentication, @PathVariable id: String): Map<String, String> {
        logger.info { "Executing Unsplash likePhoto method with id: $id" }

        val userId = principalService.getUserId(authentication)

        service.likePhoto(userId, id)

        return mapOf("message" to "Photo successfully liked")
    }

    @DeleteMapping("photo/{id}")
    suspend fun unlikePhoto(authentication: Authentication, @PathVariable id: String): Map<String, String> {
        logger.info { "Executing Unsplash unlikePhoto method with id: $id" }

        val userId = principalService.getUserId(authentication)
        service.unlikePhoto(userId, id)

        return mapOf("message" to "Photo successfully disliked")
    }
}