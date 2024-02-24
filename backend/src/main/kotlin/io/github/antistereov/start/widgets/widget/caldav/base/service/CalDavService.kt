package io.github.antistereov.start.widgets.widget.caldav.base.service

import io.github.antistereov.start.security.AESEncryption
import io.github.antistereov.start.widgets.widget.caldav.base.model.CalDavResource
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant

@Service
class CalDavService(
    private val aesEncryption: AESEncryption,
    private val calDavWidgetService: CalDavWidgetService,
    private val entityService: CalDavEntityService,
) {
    
    private val logger = LoggerFactory.getLogger(CalDavService::class.java)

    fun addResources(userId: String, resources: List<CalDavResource>): Mono<List<CalDavResource>> {
        logger.debug("Adding resources for user: $userId.")

        return calDavWidgetService.findOrCreateCalDavWidgetByUserId(userId).flatMap { widget ->
            checkForDuplicates(userId, resources).flatMap { noDuplicates ->
                if (noDuplicates) {
                    val encryptedResources = resources.map { encryptResource(it) }
                    widget.resources.addAll(encryptedResources)

                    calDavWidgetService.saveCalDavWidgetForUserId(userId, widget).map { resources }
                } else {
                    Mono.error(IllegalArgumentException("Duplicate resources found."))
                }
            }
        }
    }

    fun deleteResources(userId: String, icsLinks: List<String>): Mono<List<CalDavResource>> {
        logger.debug("Deleting resources for user: $userId.")

        return calDavWidgetService.findCalDavWidgetByUserId(userId).flatMap { widget ->
            val updatedResources = mutableListOf<CalDavResource>()

            if (icsLinks.isNotEmpty()) {
                updatedResources.addAll(
                    widget.resources.filter { aesEncryption.decrypt(it.icsLink) !in icsLinks }
                )


                widget.resources = updatedResources

                calDavWidgetService.saveCalDavWidgetForUserId(userId, widget)
                    .thenReturn(updatedResources)
            } else {
                calDavWidgetService.deleteCalDavWidget(userId).thenReturn(emptyList())
            }
        }
    }

    fun getUserResources(userId: String): Mono<List<CalDavResource>> {
        logger.debug("Getting user resources for user: $userId.")

        return calDavWidgetService.findCalDavWidgetByUserId(userId).map { widget ->
            widget.resources.map { decryptResource(it) }
        }
    }

    fun updateResourceEntities(userId: String, icsLinks: List<String>): Flux<CalDavResource> {
        logger.debug("Getting resource events of resources: {} for user: {}.", icsLinks.ifEmpty { "all" }, userId)

        return calDavWidgetService.findCalDavWidgetByUserId(userId).flatMapIterable { widget ->
            val resources = widget.resources
                .map { decryptResource(it) }.toMutableList()
            if (icsLinks.isNotEmpty()) resources.removeIf { it.icsLink !in icsLinks }

            resources
        }.flatMap { resource ->
            entityService.updateEntities(userId, resource).flatMap { updatedResource ->
                updatedResource.apply { lastUpdated = Instant.now() }
                saveUpdatedResource(userId, updatedResource)
            }
        }
    }

    private fun saveUpdatedResource(userId: String, resource: CalDavResource): Mono<CalDavResource> {
        logger.debug("Saving updated resource: ${resource.name} for user: $userId.")

        return calDavWidgetService.findCalDavWidgetByUserId(userId).flatMap { widget ->
            widget.resources = widget.resources.map {
                if (aesEncryption.decrypt(it.icsLink) == resource.icsLink) encryptResource(resource) else it
            }.toMutableList()
            calDavWidgetService.saveCalDavWidgetForUserId(userId, widget).thenReturn(resource)
        }
    }

    private fun encryptResource(resource: CalDavResource): CalDavResource {
        logger.debug("Encrypting resources.")

        return CalDavResource(
            name = aesEncryption.encrypt(resource.name),
            color = resource.color,
            icsLink = aesEncryption.encrypt(resource.icsLink),
            description = resource.description?.let { aesEncryption.encrypt(it) },
            auth = resource.auth,
            lastUpdated = resource.lastUpdated,
            readOnly = resource.readOnly,
            type = resource.type,
            entities = entityService.encryptEntities(resource.entities),
        )
    }

    private fun decryptResource(resource: CalDavResource): CalDavResource {
        logger.debug("Decrypting resource.")

        return CalDavResource(
            name = aesEncryption.decrypt(resource.name),
            color = resource.color,
            icsLink = aesEncryption.decrypt(resource.icsLink),
            description = resource.description?.let { aesEncryption.decrypt(it) },
            auth = resource.auth,
            lastUpdated = resource.lastUpdated,
            readOnly = resource.readOnly,
            type = resource.type,
            entities = entityService.decryptEntities(resource.entities),
        )
    }

    private fun checkForDuplicates(userId: String, resources: List<CalDavResource>): Mono<Boolean> {
        logger.debug("Checking for duplicates.")

        return calDavWidgetService.findCalDavWidgetByUserId(userId).map { widget ->
            val existingIcsLinks = widget.resources.map { it.icsLink }
            val decryptedIcsLinks = existingIcsLinks.map { aesEncryption.decrypt(it) }
            val duplicates = resources.find { it.icsLink in decryptedIcsLinks }

            duplicates == null
        }
    }
}