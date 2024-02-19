package io.github.antistereov.start.widgets.widget.tasks.todoist.service

import io.github.antistereov.start.global.service.BaseService
import io.github.antistereov.start.widgets.auth.todoist.config.TodoistProperties
import io.github.antistereov.start.widgets.auth.todoist.service.TodoistAuthService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class TodoistTasksService(
    private val tokenService: TodoistAuthService,
    private val baseService: BaseService,
    private val properties: TodoistProperties,
) {

    private val logger = LoggerFactory.getLogger(TodoistTasksService::class.java)

    fun getTasks(userId: String): Mono<String> {
        logger.debug("Getting tasks for user $userId.")

        val uri = "${properties.apiBaseUrl}/tasks"
        return tokenService.getAccessToken(userId).flatMap {accessToken ->
            baseService.getMono(uri, accessToken)
        }
    }
}