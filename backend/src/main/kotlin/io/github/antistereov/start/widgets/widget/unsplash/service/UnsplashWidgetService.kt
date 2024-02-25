package io.github.antistereov.start.widgets.widget.unsplash.service

import io.github.antistereov.start.user.service.UserService
import io.github.antistereov.start.widgets.widget.unsplash.model.UnsplashWidget
import io.github.antistereov.start.widgets.widget.unsplash.repository.UnsplashRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class UnsplashWidgetService(
    private val unsplashRepository: UnsplashRepository,
    private val userService: UserService,
) {

    private val logger = LoggerFactory.getLogger(UnsplashWidgetService::class.java)

    fun saveUnsplashWidgetForUserId(userId: String, widget: UnsplashWidget): Mono<UnsplashWidget> {
        logger.debug("Saving UnsplashWidget for user: $userId.")


        return userService.findById(userId).flatMap { user ->
            val unsplashId = user.widgets.unsplashId

            if (unsplashId == null) {
                saveUnsplashWidget(widget).flatMap { widget ->
                    user.widgets.unsplashId = widget.id
                    userService.save(user).thenReturn(widget)
                }
            } else {
                saveUnsplashWidget(widget)
            }
        }
    }

    fun findUnsplashWidgetByUserId(userId: String): Mono<UnsplashWidget> {
        logger.debug("Finding UnsplashWidget by user ID: $userId.")

        return userService.findById(userId).flatMap { user ->
            val unsplashId = user.widgets.unsplashId
                ?: return@flatMap Mono.error(IllegalArgumentException("No UnsplashWidget ID found for user: $userId"))
            findUnsplashWidgetById(unsplashId)
        }
    }

    fun findOrCreateUnsplashWidgetByUserId(userId: String): Mono<UnsplashWidget> {
        return userService.findById(userId).flatMap { user ->
            val unsplashId = user.widgets.unsplashId

            if (unsplashId == null) {
                val newWidget = UnsplashWidget()
                saveUnsplashWidget(newWidget).flatMap { widget ->
                    user.widgets.unsplashId = widget.id
                    userService.save(user).thenReturn(widget)
                }
            } else {
                findUnsplashWidgetById(unsplashId)
            }
        }
    }

    fun deleteUnsplashWidget(userId: String): Mono<String> {
        logger.debug("Deleting Unsplash widget for user: $userId.")

        return userService.deleteUnsplashWidget(userId)
    }

    private fun findUnsplashWidgetById(widgetId: String?): Mono<UnsplashWidget> {
        logger.debug("Finding UnsplashWidget by ID: $widgetId.")

        if (widgetId == null) {
            return Mono.error(IllegalArgumentException("No UnsplashWidget ID provided."))
        }

        return unsplashRepository.findById(widgetId)
            .switchIfEmpty(Mono.error(IllegalArgumentException("UnsplashWidget not found with ID: $widgetId")))
    }

    private fun saveUnsplashWidget(unsplashWidget: UnsplashWidget): Mono<UnsplashWidget> {
        logger.debug("Saving UnsplashWidget.")

        return unsplashRepository.save(unsplashWidget)
            .onErrorMap { error ->
                logger.error("Error saving UnsplashWidget for user.", error)
                error
            }
    }
}