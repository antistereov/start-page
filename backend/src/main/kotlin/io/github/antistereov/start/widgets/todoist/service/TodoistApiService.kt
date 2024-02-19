package io.github.antistereov.start.widgets.todoist.service

import io.github.antistereov.start.global.service.BaseService
import io.github.antistereov.start.widgets.todoist.config.TodoistProperties
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class TodoistApiService(
    private val tokenService: TodoistTokenService,
    private val baseService: BaseService,
    private val properties: TodoistProperties,
) {

    fun getTasks(userId: String): Mono<String> {
        val uri = "${properties.apiBaseUrl}/tasks"
        return tokenService.getAccessToken(userId).flatMap {accessToken ->
            baseService.getMono(uri, accessToken)
        }
    }
}