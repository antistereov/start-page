package io.github.antistereov.start.widgets.todoist.service

import io.github.antistereov.start.global.service.BaseService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class TodoistApiService(
    private val tokenService: TodoistTokenService,
    private val baseService: BaseService,
) {

    @Value("\${todoist.apiBaseUrl}")
    private lateinit var apiBaseUrl: String

    fun getTasks(userId: String): Mono<String> {
        val uri = "$apiBaseUrl/tasks"
        return tokenService.getAccessToken(userId).flatMap {accessToken ->
            baseService.makeAuthorizedGetRequest(uri, accessToken)
        }
    }
}