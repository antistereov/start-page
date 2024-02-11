package io.github.antistereov.start.widgets.nextcloud.service

import io.github.antistereov.start.widgets.nextcloud.model.NewsItem
import io.github.antistereov.start.widgets.nextcloud.model.NextcloudCredentials
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Flux
import java.util.*

@Service
class NewsService(
    private val webClientBuilder: WebClient.Builder,
) {

    fun getLatestNews(
        credentials: NextcloudCredentials,
        batchSize: Int = 30
    ): Flux<NewsItem> {
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
            .bodyToFlux(NewsItem::class.java)
    }
}