package io.github.antistereov.start.connector.unsplash

import io.github.antistereov.start.auth.service.PrincipalService
import io.github.antistereov.start.connector.unsplash.model.UnsplashPhoto
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ServerWebExchange

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
        exchange: ServerWebExchange,
        @RequestParam screenWidth: Int,
        @RequestParam screenHeight: Int,
        @RequestParam quality: Int = 75,
    ): ResponseEntity<UnsplashPhoto> {
        logger.info { "Executing Unsplash getRandomPhoto" }

        val userId = principalService.getUserId(exchange)

        return ResponseEntity.ok(
            service.getRandomPhoto(userId, screenWidth, screenHeight, quality)
        )
    }

    @GetMapping("/photo/{id}")
    suspend fun getPhoto(
        exchange: ServerWebExchange,
        @PathVariable id: String,
        @RequestParam screenWidth: Int,
        @RequestParam screenHeight: Int,
        @RequestParam quality: Int = 75,
    ): ResponseEntity<UnsplashPhoto> {
        logger.info { "Executing Unsplash getPhoto method with id: $id" }

        val userId = principalService.getUserId(exchange)

        return ResponseEntity.ok(
            service.getPhoto(userId, id, screenWidth, screenHeight, quality)
        )
    }

    @PostMapping("photo/{id}")
    suspend fun likePhoto(
        exchange: ServerWebExchange,
        @PathVariable id: String
    ): ResponseEntity<Map<String, String>> {
        logger.info { "Executing Unsplash likePhoto method with id: $id" }

        val userId = principalService.getUserId(exchange)

        service.likePhoto(userId, id)

        return ResponseEntity.ok(
            mapOf("message" to "Photo successfully liked")
        )
    }

    @DeleteMapping("photo/{id}")
    suspend fun unlikePhoto(exchange: ServerWebExchange,
                            @PathVariable id: String): ResponseEntity<Map<String, String>> {
        logger.info { "Executing Unsplash unlikePhoto method with id: $id" }

        val userId = principalService.getUserId(exchange)
        service.unlikePhoto(userId, id)

        return ResponseEntity.ok(
            mapOf("message" to "Photo successfully disliked")
        )
    }
}