package io.github.antistereov.start.widgets.widget.caldav.controller

import io.github.antistereov.start.auth.service.PrincipalService
import io.github.antistereov.start.widgets.widget.caldav.dto.CreateCalDavResourceDTO
import io.github.antistereov.start.widgets.widget.caldav.dto.UpdateCalDavResourceDTO
import io.github.antistereov.start.widgets.widget.caldav.model.CalDavResource
import io.github.antistereov.start.widgets.widget.caldav.service.CalDavService
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/caldav")
class CalDavController(
    private val calDavService: CalDavService,
    private val principalExtractor: PrincipalService,
) {

    private val logger = LoggerFactory.getLogger(CalDavController::class.java)

    @GetMapping
    fun getUserResources(authentication: Authentication): Flux<CalDavResource> {
        logger.info("Getting user resources.")

        return principalExtractor.getUserId(authentication).flatMapMany { userId ->
            calDavService.getUserResources(userId)
        }
    }

    @PostMapping
    fun createResource(
        authentication: Authentication,
        @RequestBody resource: CreateCalDavResourceDTO,
    ): Mono<CalDavResource> {
        logger.info("Creating resource: ${resource.icsLink}")

        return principalExtractor.getUserId(authentication).flatMap { userId ->
            calDavService.createResource(userId, resource)
        }
    }

    @PutMapping
    fun updateResource(
        authentication: Authentication,
        @RequestBody resource: UpdateCalDavResourceDTO,
    ): Mono<CalDavResource> {
        logger.info("Updating resource: ${resource.id}")

        return principalExtractor.getUserId(authentication).flatMap { userId ->
            calDavService.updateResource(userId, resource)
        }
    }

    @DeleteMapping
    fun deleteResources(
        authentication: Authentication,
        @RequestParam(required = true) id: String,
    ): Mono<String> {
        logger.info("Deleting resources.")

        return principalExtractor.getUserId(authentication).flatMap { userId ->
            calDavService.deleteResource(userId, id)
        }
    }

    @PutMapping("/refresh")
    fun refreshResourceEntities(
        authentication: Authentication,
    ): Flux<CalDavResource> {
        logger.info("Updating resource entities.")

        return principalExtractor.getUserId(authentication).flatMapMany { userId ->
            calDavService.refreshResourceEntities(userId)
        }
    }
}