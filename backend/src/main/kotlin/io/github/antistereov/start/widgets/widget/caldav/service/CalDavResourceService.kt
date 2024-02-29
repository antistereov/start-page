package io.github.antistereov.start.widgets.widget.caldav.service

import io.github.antistereov.start.global.exception.CannotDeleteDocumentException
import io.github.antistereov.start.global.exception.CannotSaveDocumentException
import io.github.antistereov.start.global.exception.DocumentNotFoundException
import io.github.antistereov.start.security.AESEncryption
import io.github.antistereov.start.widgets.widget.caldav.model.CalDavEntity
import io.github.antistereov.start.widgets.widget.caldav.model.CalDavResource
import io.github.antistereov.start.widgets.widget.caldav.repository.CalDavResourceRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class CalDavResourceService(
    private val repository: CalDavResourceRepository,
    private val aesEncryption: AESEncryption,
) {

    private val logger = LoggerFactory.getLogger(CalDavResourceService::class.java)

    fun findById(id: String): Mono<CalDavResource> {
        return repository.findById(id)
            .switchIfEmpty(Mono.error(DocumentNotFoundException(id, CalDavResource::class.java)))
            .map { encryptedResource ->
                decryptResource(encryptedResource)
            }
    }

    fun saveCalDavResource(decryptedResource: CalDavResource): Mono<CalDavResource> {
        logger.debug("Adding resource: ${decryptedResource.icsLink}")

        val encryptedResource = encryptResource(decryptedResource)

        return repository.save(encryptedResource)
            .onErrorMap { ex ->
                CannotSaveDocumentException(decryptedResource.icsLink, CalDavResource::class.java, ex)
            }
    }

    fun deleteCalDavResourceById(id: String): Mono<String> {
        logger.debug("Deleting resource with id: $id")

        return repository.deleteById(id)
            .onErrorMap { ex ->
                CannotDeleteDocumentException(id, CalDavResource::class.java, ex)
            }
            .thenReturn("Resource with id: $id deleted")
    }

    private fun encryptResource(resource: CalDavResource): CalDavResource {
        logger.debug("Encrypting resources.")

        return CalDavResource(
            id = resource.id,
            name = aesEncryption.encrypt(resource.name),
            color = resource.color,
            icsLink = aesEncryption.encrypt(resource.icsLink),
            description = resource.description?.let { aesEncryption.encrypt(it) },
            auth = resource.auth,
            lastUpdated = resource.lastUpdated,
            readOnly = resource.readOnly,
            type = resource.type,
            entities = encryptEntities(resource.entities),
        )
    }

    private fun encryptEntities(entities: MutableList<CalDavEntity>): MutableList<CalDavEntity> {
        logger.debug("Encrypting entities.")

        return entities.map { entity ->
            entity.copy(
                summary = aesEncryption.encrypt(entity.summary),
                description = entity.description?.let { aesEncryption.encrypt(it) },
                location = entity.location?.let { aesEncryption.encrypt(it) },
            )
        }.toMutableList()
    }

    private fun decryptResource(resource: CalDavResource): CalDavResource {
        logger.debug("Decrypting resource.")

        return CalDavResource(
            id = resource.id,
            name = aesEncryption.decrypt(resource.name),
            color = resource.color,
            icsLink = aesEncryption.decrypt(resource.icsLink),
            description = resource.description?.let { aesEncryption.decrypt(it) },
            auth = resource.auth,
            lastUpdated = resource.lastUpdated,
            readOnly = resource.readOnly,
            type = resource.type,
            entities = decryptEntities(resource.entities),
        )
    }

    private fun decryptEntities(entities: List<CalDavEntity>): MutableList<CalDavEntity> {
        logger.debug("Decrypting entities.")

        return entities.map { entity ->
            entity.copy(
                summary = aesEncryption.decrypt(entity.summary),
                description = entity.description?.let { aesEncryption.decrypt(it) },
                location = entity.location?.let { aesEncryption.decrypt(it) },
            )
        }.toMutableList()
    }
}