package io.github.antistereov.start.widgets.nextcloud.service

import io.github.antistereov.start.widgets.nextcloud.model.NextcloudCredentials
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import java.util.*

@Service
class NewsService(
    private val webClientBuilder: WebClient.Builder,
) {

    fun getLatestNews(
        nextcloudCredentials: NextcloudCredentials,
        batchSize: Int = 30
    ): Mono<String> {
        val uri = UriComponentsBuilder.fromHttpUrl("${nextcloudCredentials.url}/index.php/apps/news/api/v1-3/items")
            .queryParam("batchSize", batchSize)
            .toUriString()

        val authHeaderValue = Base64.getEncoder()
            .encodeToString("${nextcloudCredentials.username}:${nextcloudCredentials.password}".toByteArray())
        return webClientBuilder.build()
            .get()
            .uri(uri)
            .header("Authorization", "Basic $authHeaderValue")
            .retrieve()
            .bodyToMono(String::class.java)
    }
}