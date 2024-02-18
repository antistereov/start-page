package io.github.antistereov.start.widgets.nextcloud.controller

import io.github.antistereov.start.security.AuthenticationPrincipalExtractor
import io.github.antistereov.start.widgets.nextcloud.service.NextcloudAuthService
import io.github.antistereov.start.widgets.nextcloud.service.NewsService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/widgets/nextcloud/news")
class NewsController(
    private val newsService: NewsService,
    private val principalExtractor: AuthenticationPrincipalExtractor,
    private val nextcloudAuthService: NextcloudAuthService,
) {

    val logger: Logger = LoggerFactory.getLogger(NewsController::class.java)

    @GetMapping("/items")
    fun getLatestNews(
        authentication: Authentication,
        @RequestParam batchSize: Int = 30
    ): Mono<String> {
        logger.info("Executing Nextcloud getLatestNews method.")

        return principalExtractor.getUserId(authentication)
            .flatMap { userId ->
                nextcloudAuthService.getCredentials(userId)
                    .flatMap { credentials ->
                        newsService.getLatestNews(credentials, batchSize)
                    }
            }
    }

}