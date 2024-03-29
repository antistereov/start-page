package io.github.antistereov.start.global.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
class BaseService(
    private val webClient: WebClient,
) {

    private val logger = LoggerFactory.getLogger(BaseService::class.java)

    fun getMono(
        uri: String,
        bearer: String? = null,
        basic: String? = null,
    ): Mono<String> {
        logger.debug("GET $uri, bearer=${bearer != null}, basic=${basic != null}")

        if (bearer != null && basic != null) {
            throw IllegalArgumentException("Both Bearer and Basic authentication cannot be non-null.")
        }

        return webClient
            .get()
            .uri(uri).apply {
                if (bearer != null) {
                    this.headers { it.setBearerAuth(bearer) }
                } else if (basic != null) {
                    this.headers { it.setBasicAuth(basic) }
                }
            }
            .retrieve()
            .bodyToMono(String::class.java)
    }

    fun postMono(
        uri: String,
        bearer: String? = null,
        basic: String? = null,
        body: Any? = null,
    ): Mono<String> {
        logger.debug("POST {}, bearer={}, basic={}, body={}", uri, bearer != null, basic != null, body)

        if (bearer != null && basic != null) {
            throw IllegalArgumentException("Both Bearer and Basic authentication cannot be non-null.")
        }

        return webClient
            .post()
            .uri(uri).apply {
                if (bearer != null) {
                    this.headers { it.setBearerAuth(bearer) }
                } else if (basic != null) {
                    this.headers { it.setBasicAuth(basic) }
                }
            }
            .body(body?.let { BodyInserters.fromValue(it) } ?: BodyInserters.empty<Any>())
            .retrieve()
            .bodyToMono(String::class.java)
    }

    fun deleteMono(
        uri: String,
        bearer: String? = null,
        basic: String? = null,
    ): Mono<String> {
        logger.debug("GET $uri, bearer=${bearer != null}, basic=${basic != null}")

        if (bearer != null && basic != null) {
            throw IllegalArgumentException("Both Bearer and Basic authentication cannot be non-null.")
        }

        return webClient
            .delete()
            .uri(uri).apply {
                if (bearer != null) {
                    this.headers { it.setBearerAuth(bearer) }
                } else if (basic != null) {
                    this.headers { it.setBasicAuth(basic) }
                }
            }
            .retrieve()
            .bodyToMono(String::class.java)
    }

    fun extractField(response: String, vararg fields: String): String {
        logger.debug("Extracting field {} from the JSON response", fields)

        var json: JsonNode = jacksonObjectMapper().readTree(response)
        for (field in fields) {
            json = json.get(field) ?: throw IllegalArgumentException("Field $field not found in the JSON response")
        }
        return json.asText()
    }
}
