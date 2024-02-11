package io.github.antistereov.start.widgets.nextcloud.controller

import io.github.antistereov.start.security.AuthenticationPrincipalExtractor
import io.github.antistereov.start.widgets.nextcloud.model.NewsItem
import io.github.antistereov.start.widgets.nextcloud.service.AuthService
import io.github.antistereov.start.widgets.nextcloud.service.NewsService
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/api/nextcloud/news")
class NewsController(
    private val newsService: NewsService,
    private val principalExtractor: AuthenticationPrincipalExtractor,
    private val authService: AuthService,
) {

    @GetMapping("/items")
    fun getLatestNews(
        authentication: Authentication,
        @RequestParam batchSize: Int = 30
    ): Flux<NewsItem> {
        return principalExtractor.getUserId(authentication)
            .flatMapMany { userId ->
                authService.getCredentials(userId)
                    .flatMapMany { credentials ->
                        newsService.getLatestNews(credentials, batchSize)
                    }
            }
    }

}