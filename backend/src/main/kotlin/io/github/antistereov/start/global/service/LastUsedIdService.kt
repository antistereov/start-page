package io.github.antistereov.start.global.service

import io.github.antistereov.start.global.model.LastUsedId
import io.github.antistereov.start.global.respository.LastUsedIdRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class LastUsedIdService(
    private val lastUsedIdRepository: LastUsedIdRepository
) {
    fun getAndUpdateLastUsedId(collection: String): Mono<Long> {
        return getLastUsedId(collection)
            .flatMap { lastUsedId ->
                updateLastUsedId(collection, lastUsedId + 1)
                    .thenReturn(lastUsedId + 1)
            }
    }

    private fun getLastUsedId(collection: String): Mono<Long> {
        return lastUsedIdRepository.findById(collection)
            .switchIfEmpty(lastUsedIdRepository.save(LastUsedId(collection, 0)))
            .map { it.lastUsedId }
    }

    private fun updateLastUsedId(collection: String, lastUsedId: Long): Mono<LastUsedId> {
        return lastUsedIdRepository.findById(collection)
            .flatMap { lastUsedIdDocument ->
                lastUsedIdDocument.lastUsedId = lastUsedId
                lastUsedIdRepository.save(lastUsedIdDocument)
            }
    }
}