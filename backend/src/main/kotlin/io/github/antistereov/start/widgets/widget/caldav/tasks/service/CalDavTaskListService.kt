package io.github.antistereov.start.widgets.widget.caldav.tasks.service

import io.github.antistereov.start.security.AESEncryption
import io.github.antistereov.start.widgets.widget.caldav.base.model.CalDavResource
import io.github.antistereov.start.widgets.widget.caldav.base.model.CalDavResourceType
import io.github.antistereov.start.widgets.widget.caldav.base.service.CalDavEntityService
import io.github.antistereov.start.widgets.widget.caldav.base.service.CalDavService
import io.github.antistereov.start.widgets.widget.caldav.base.service.CalDavWidgetService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class CalDavTaskListService(
    aesEncryption: AESEncryption,
    calDavWidgetService: CalDavWidgetService,
    entityService: CalDavEntityService,
) : CalDavService(aesEncryption, calDavWidgetService, entityService) {

    private val logger = LoggerFactory.getLogger(CalDavTaskListService::class.java)

    fun getUserTaskLists(userId: String): Mono<List<CalDavResource>> {
        logger.debug("Getting user task lists for user: $userId.")

        return super.getUserResources(userId)
            .map { resources ->
                resources.filter { it.type == CalDavResourceType.TaskList }
            }
    }
}