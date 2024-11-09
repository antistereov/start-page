package io.github.antistereov.start.widget.unsplash

import io.github.antistereov.start.auth.service.PrincipalService
import io.github.antistereov.start.widget.unsplash.auth.UnsplashAuthController
import io.github.antistereov.start.widget.unsplash.auth.UnsplashAuthService
import io.github.antistereov.start.widget.unsplash.auth.model.UnsplashPublicUserProfile
import io.github.antistereov.start.widget.unsplash.model.UnsplashPhoto
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/unsplash")
class UnsplashController(
    private val service: UnsplashService,
    private val principalService: PrincipalService,
    private val unsplashAuthService: UnsplashAuthService,
) {

    private val logger: KLogger
        get() = KotlinLogging.logger {}

    @GetMapping("/photo")
    suspend fun getRandomPhoto(authentication: Authentication, @RequestParam query: String? = null): UnsplashPhoto {
        logger.info { "Executing Unsplash getRandomPhoto with method query: ${query}." }

        val userId = principalService.getUserId(authentication)

        return service.getRandomPhoto(userId, query)
    }

    @GetMapping("/photo/url")
    suspend fun getRandomPhotoUrlForScreen(
        authentication: Authentication,
        @RequestParam(required = false) query: String? = null,
        @RequestParam(required = true) width: Int,
        @RequestParam(required = true) height: Int,
        @RequestParam(required = false) quality: Int = 85,
    ): String {
        logger.info { "Executing Unsplash getRandomPhotoUrlForScreen method with query: ${query}, " +
                "width: ${width}, height: ${height}, quality: ${quality}." }

        val userId = principalService.getUserId(authentication)

        return service.getNewRandomPhotoUrlForScreen(userId, query, width, height, quality)
    }

    @GetMapping("/photo/{id}")
    suspend fun getPhoto(@PathVariable id: String): UnsplashPhoto {
        logger.info { "Executing Unsplash getPhoto method with id: ${id}." }

        return service.getPhoto(id)
    }

    @PostMapping("photo/{id}/like")
    suspend fun likePhoto(authentication: Authentication, @PathVariable id: String): String {
        logger.info { "Executing Unsplash likePhoto method with id: ${id}." }

        val userId = principalService.getUserId(authentication)

        return service.likePhoto(userId, id)
    }

    @DeleteMapping("photo/{id}/like")
    suspend fun unlikePhoto(authentication: Authentication, @PathVariable id: String): String {
        logger.info { "Executing Unsplash unlikePhoto method with id: ${id}." }

        val userId = principalService.getUserId(authentication)
        return service.unlikePhoto(userId, id)
    }
}