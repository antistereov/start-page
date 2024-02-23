package io.github.antistereov.start.widgets.widget.caldav.base.service

import io.github.antistereov.start.global.service.LastUsedIdService
import io.github.antistereov.start.user.service.UserService
import io.github.antistereov.start.widgets.widget.caldav.model.CalDavWidget
import io.github.antistereov.start.widgets.widget.caldav.repository.CalDavRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class CalDavWidgetService(
    private val calDavRepository: CalDavRepository,
    private val userService: UserService,
    private val idService: LastUsedIdService,
) {

    private val logger = LoggerFactory.getLogger(CalDavWidgetService::class.java)

    fun saveCalDavWidgetForUserId(userId: String, widget: CalDavWidget): Mono<CalDavWidget> {
        logger.debug("Saving CalDavWidget for user: $userId.")


        return userService.findById(userId).flatMap { user ->
            val calDavId = user.widgets.calDavId

            if (calDavId == null) {
                saveCalDavWidget(widget).flatMap { widget ->
                    user.widgets.calDavId = widget.id
                    userService.save(user).thenReturn(widget)
                }
            } else {
                saveCalDavWidget(widget)
            }
        }
    }

    fun findCalDavWidgetByUserId(userId: String): Mono<CalDavWidget> {
        logger.debug("Finding CalDavWidget by user ID: $userId.")

        return userService.findById(userId).flatMap { user ->
            val calDavId = user.widgets.calDavId
                ?: return@flatMap Mono.error(IllegalArgumentException("No CalDavWidget ID found for user: $userId"))
            findCalDavWidgetById(calDavId)
        }
    }

    fun findOrCreateCalDavWidgetByUserId(userId: String): Mono<CalDavWidget> {
        return userService.findById(userId).flatMap { user ->
            val calDavId = user.widgets.calDavId

            if (calDavId == null) {
                generateId().flatMap { id ->
                    val newWidget = CalDavWidget(id)
                    saveCalDavWidget(newWidget).flatMap { widget ->
                        user.widgets.calDavId = widget.id
                        userService.save(user).thenReturn(widget)
                    }
                }
            } else {
                findCalDavWidgetById(calDavId)
            }
        }
    }

    fun deleteCalDavWidget(userId: String): Mono<String> {
        logger.debug("Deleting CalDav widget for user: $userId.")

        return userService.findById(userId).flatMap { user ->
            if (user.widgets.calDavId == null) {
                return@flatMap Mono.just("CalDav widget cleared for user: $userId.")
            }
            findCalDavWidgetById(user.widgets.calDavId).flatMap { widget ->
                calDavRepository.delete(widget)
                    .doOnError { error ->
                        logger.error("Error deleting CalDav widget for user: $userId.", error)
                    }
                    .then(userService.save(user.apply { this.widgets.calDavId = null }))
                    .map {"CalDav widget cleared for user: $userId." }
            }
        }
    }

    private fun findCalDavWidgetById(widgetId: Long?): Mono<CalDavWidget> {
        logger.debug("Finding CalDavWidget by ID: $widgetId.")

        if (widgetId == null) {
            return Mono.error(IllegalArgumentException("No CalDavWidget ID provided."))
        }

        return calDavRepository.findById(widgetId)
            .switchIfEmpty(Mono.error(IllegalArgumentException("CalDavWidget not found with ID: $widgetId")))
    }

    private fun saveCalDavWidget(calDavWidget: CalDavWidget): Mono<CalDavWidget> {
        logger.debug("Saving CalDavWidget.")

        return calDavRepository.save(calDavWidget)
            .onErrorMap { error ->
                logger.error("Error saving CalDavWidget for user.", error)
                error
            }
    }

    private fun generateId(): Mono<Long> {
        return idService.getAndUpdateLastUsedId("caldav")
    }
}