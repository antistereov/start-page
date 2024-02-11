package io.github.antistereov.start.widgets.nextcloud.controller

import io.github.antistereov.start.widgets.nextcloud.service.AuthService
import io.github.antistereov.start.widgets.nextcloud.service.NewsService
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/nextcloud/news")
class NewsController(
    private val newsService: NewsService,
    private val authService: AuthService,
) {

    @GetMapping("/items")
    fun getLatestNews(
        authentication: Authentication,
        @RequestParam batchSize: Int = 30
    ): Mono<String> {
        val principal = authentication.principal as Jwt
        val userId = principal.claims["sub"].toString()
        val nextcloudCredentials = authService.getCredentials(userId)

        return newsService.getLatestNews(nextcloudCredentials, batchSize)
    }

}