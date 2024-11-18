package io.github.antistereov.start.connector.unsplash

import io.github.antistereov.start.auth.service.PrincipalService
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
        @RequestParam params: Map<String, Any?> = emptyMap()
    ): String {
        logger.info { "Executing Unsplash getRandomPhoto" }

        val userId = principalService.getUserId(authentication)

        return service.getRandomPhoto(userId, params)
    }

    @GetMapping("/photo/{id}")
    suspend fun getPhoto(
        authentication: Authentication,
        @PathVariable id: String
    ): String {
        logger.info { "Executing Unsplash getPhoto method with id: ${id}" }

        val userId = principalService.getUserId(authentication)

        return service.getPhoto(userId, id)
    }

    @PostMapping("photo/{id}")
    suspend fun likePhoto(authentication: Authentication, @PathVariable id: String): String {
        logger.info { "Executing Unsplash likePhoto method with id: $id" }

        val userId = principalService.getUserId(authentication)

        return service.likePhoto(userId, id)
    }

    @DeleteMapping("photo/{id}")
    suspend fun unlikePhoto(authentication: Authentication, @PathVariable id: String): String {
        logger.info { "Executing Unsplash unlikePhoto method with id: $id" }

        val userId = principalService.getUserId(authentication)
        return service.unlikePhoto(userId, id)
    }
}