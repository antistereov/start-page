package io.github.antistereov.start.widgets.widget.news.nextcloud.service

import io.github.antistereov.start.widgets.auth.nextcloud.model.NextcloudCredentials
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import java.util.*

@Service
class NewsService(
    private val webClientBuilder: WebClient.Builder,
) {

    private val logger = LoggerFactory.getLogger(NewsService::class.java)

    fun getLatestNews(
        credentials: NextcloudCredentials,
        batchSize: Int = 30
    ): Mono<String> {
        logger.debug("Getting latest news.")

        val uri = UriComponentsBuilder.fromHttpUrl("${credentials.url}/index.php/apps/news/api/v1-3/items")
            .queryParam("batchSize", batchSize)
            .toUriString()

        val authHeaderValue = Base64.getEncoder()
            .encodeToString("${credentials.username}:${credentials.password}".toByteArray())
        return webClientBuilder.build()
            .get()
            .uri(uri)
            .header("Authorization", "Basic $authHeaderValue")
            .retrieve()
            .bodyToMono(String::class.java)
    }
}