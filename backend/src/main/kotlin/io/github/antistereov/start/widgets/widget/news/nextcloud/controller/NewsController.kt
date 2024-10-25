package io.github.antistereov.start.widgets.widget.news.nextcloud.controller

import io.github.antistereov.start.auth.service.PrincipalService
import io.github.antistereov.start.widgets.auth.nextcloud.service.NextcloudAuthService
import io.github.antistereov.start.widgets.widget.news.nextcloud.service.NewsService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/news/nextcloud")
class NewsController(
    private val newsService: NewsService,
    private val principalExtractor: PrincipalService,
    private val nextcloudAuthService: NextcloudAuthService,
) {

    private val logger: Logger = LoggerFactory.getLogger(NewsController::class.java)

    @GetMapping("/items")
    fun getLatestNews(
        authentication: Authentication,
        @RequestParam batchSize: Int = 30
    ): Mono<String> {
        logger.info("Getting latest news.")

        return principalExtractor.getUserId(authentication)
            .flatMap { userId ->
                nextcloudAuthService.getCredentials(userId)
                    .flatMap { credentials ->
                        newsService.getLatestNews(credentials, batchSize)
                    }
            }
    }

}