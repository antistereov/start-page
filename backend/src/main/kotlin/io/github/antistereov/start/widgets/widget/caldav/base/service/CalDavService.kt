package io.github.antistereov.start.widgets.widget.caldav.base.service

import io.github.antistereov.start.security.AESEncryption
import io.github.antistereov.start.user.model.User
import io.github.antistereov.start.user.service.UserService
import io.github.antistereov.start.widgets.widget.caldav.base.model.CalDavResource
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant

open class CalDavService(
    private val userService: UserService,
    private val aesEncryption: AESEncryption,
    private val entityService: CalDavEntityService,
) {
    
    private val logger = LoggerFactory.getLogger(CalDavService::class.java)

    fun addResources(userId: String, resources: List<CalDavResource>): Mono<List<CalDavResource>> {
        logger.debug("Adding resources for user: $userId.")

        return userService.findById(userId).flatMap { user ->
            checkForDuplicates(user, resources)

            val encryptedResources = resources.map { encryptResource(it) }
            user.widgets.calDav.resources.addAll(encryptedResources)

            userService.save(user).map { resources }
        }
    }

    fun deleteResources(userId: String, icsLinks: List<String>): Mono<List<CalDavResource>> {
        logger.debug("Deleting resources for user: $userId.")

        return userService.findById(userId).flatMap { user ->
            val updatedResources = mutableListOf<CalDavResource>()

            if (icsLinks.isNotEmpty()) {
                updatedResources.addAll(
                    user.widgets.calDav.resources.filter { aesEncryption.decrypt(it.icsLink) !in icsLinks }
                )
            }

            user.widgets.calDav.resources = updatedResources

            userService.save(user).map { updatedUser ->
                updatedUser.widgets.calDav.resources
            }
        }
    }

    fun getUserResources(userId: String): Mono<List<CalDavResource>> {
        logger.debug("Getting user resources for user: $userId.")

        return userService.findById(userId).map { user ->
            user.widgets.calDav.resources.map { resource ->
                decryptResource(resource)
            }
        }
    }

    fun updateResourceEntities(userId: String, icsLinks: List<String>): Flux<CalDavResource> {
        logger.debug("Getting resource events of resources: {} for user: {}.", icsLinks.ifEmpty { "all" }, userId)

        return userService.findById(userId).flatMapIterable { user ->
            val resources = user.widgets.calDav.resources
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

        return userService.findById(userId).flatMap { user ->
            user.widgets.calDav.resources = user.widgets.calDav.resources.map {
                if (aesEncryption.decrypt(it.icsLink) == resource.icsLink) encryptResource(resource) else it
            }.toMutableList()
            userService.save(user).map { resource }
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

    private fun checkForDuplicates(user: User, resources: List<CalDavResource>) {
        logger.debug("Checking for duplicates.")

        val existingIcsLinks = user.widgets.calDav.resources.map { it.icsLink }
        val decryptedIcsLinks = existingIcsLinks.map { aesEncryption.decrypt(it) }
        val duplicates = resources.find { it.icsLink in decryptedIcsLinks }
        if (duplicates != null) {
            throw IllegalArgumentException("Trying to add existing resources: $duplicates")
        }
    }
}