package io.github.antistereov.start.widgets.widget.caldav.base.service

import io.github.antistereov.start.global.exception.DocumentExistsException
import io.github.antistereov.start.user.service.UserService
import io.github.antistereov.start.widgets.widget.caldav.base.dto.CreateCalDavResourceDTO
import io.github.antistereov.start.widgets.widget.caldav.base.dto.UpdateCalDavResourceDTO
import io.github.antistereov.start.widgets.widget.caldav.base.model.CalDavResource
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class CalDavService(
    private val resourceService: CalDavResourceService,
    private val userService: UserService,
    private val entityService: CalDavEntityService,
) {
    
    private val logger = LoggerFactory.getLogger(CalDavService::class.java)

    fun createResource(userId: String, resourceDTO: CreateCalDavResourceDTO): Mono<CalDavResource> {
        logger.debug("Creating resource: ${resourceDTO.icsLink} for user: $userId")

        val icsLink = resourceDTO.icsLink

        return resourceWithIcsLinkExists(userId, icsLink).flatMap { exists ->
            if (exists) return@flatMap Mono.error(DocumentExistsException(icsLink, CalDavResource::class.java))

            saveResource(userId, resourceDTO.toCalDavResource())
        }
    }

    fun updateResource(userId: String, resourceDTO: UpdateCalDavResourceDTO): Mono<CalDavResource> {
        logger.debug("Updating resource: ${resourceDTO.id} for user: $userId")

        return saveResource(userId, resourceDTO.toCalDavResource())
    }

    private fun saveResource(userId: String, calDavResource: CalDavResource): Mono<CalDavResource> {
        logger.debug("Creating resource: ${calDavResource.icsLink} for user: $userId")

        return resourceService.saveCalDavResource(calDavResource).flatMap { savedCalDavResource ->
            userService.findById(userId).flatMap { user ->
                user.widgets.calDav.resources.add(savedCalDavResource.id!!)
                userService.save(user)
                    .thenReturn(calDavResource)
            }
        }
    }

    private fun resourceWithIcsLinkExists(userId: String, newIcsLink: String): Mono<Boolean> {
        logger.debug("Checking if resource with ICS link: $newIcsLink exists for user: $userId")

        return getUserResources(userId).any { it.icsLink == newIcsLink }
    }

    fun deleteResource(userId: String, resourceId: String): Mono<String> {
        logger.debug("Deleting resource: $resourceId for user: $userId.")

        return resourceService.deleteCalDavResourceById(resourceId).flatMap { message ->
            userService.findById(userId).flatMap { user ->
                user.widgets.calDav.resources.remove(resourceId)
                userService.save(user).thenReturn(message)
            }
        }
    }

    fun getUserResources(userId: String): Flux<CalDavResource> {
        logger.debug("Getting user resources for user: $userId.")

        return userService.findById(userId).flatMapMany { user ->
            val resourceIds = user.widgets.calDav.resources

            Flux.fromIterable(resourceIds).flatMap { resourceId ->
                resourceService.findById(resourceId)
            }
        }
    }


    fun refreshResourceEntities(userId: String): Flux<CalDavResource> {
        logger.debug("Getting resource events for user: {}.", userId)

        return userService.findById(userId).flatMapMany { user ->
            val resourceIds = user.widgets.calDav.resources

            Flux.fromIterable(resourceIds).flatMap { resourceId ->
                refreshResourceEntities(userId, resourceId)
            }
        }
    }

    private fun refreshResourceEntities(userId: String, resourceId: String): Mono<CalDavResource> {
        logger.debug("Updating resource entities for resource: $resourceId")

        return resourceService.findById(resourceId).flatMap { resource ->
            entityService.updateEntities(userId, resource)
        }
    }
}