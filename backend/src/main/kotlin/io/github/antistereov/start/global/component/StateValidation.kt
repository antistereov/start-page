package io.github.antistereov.start.global.component

import io.github.antistereov.start.global.model.exception.InvalidStateParameterException
import io.github.antistereov.start.security.AESEncryption
import io.github.antistereov.start.global.model.StateParameter
import io.github.antistereov.start.user.repository.StateRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.Instant

@Component
class StateValidation(
    private val aesEncryption: AESEncryption,
    private val stateRepository: StateRepository,
) {

    val logger: Logger = LoggerFactory.getLogger(StateValidation::class.java)
    val validityPeriodMinutes = 5L

    fun createState(userId: String): Mono<String> {
        val timestamp = Instant.now().toEpochMilli()
        val state = "$userId:$timestamp"
        val encryptedState = aesEncryption.encrypt(state)
        return stateRepository.save(StateParameter(encryptedState, userId, timestamp)).map { encryptedState }
    }

    fun getUserId(state: String): Mono<String> {
        val decryptedState = aesEncryption.decrypt(state)
        return stateRepository.findById(state)
            .switchIfEmpty(Mono.error(InvalidStateParameterException()))
            .flatMap { storedState ->
                validateState(decryptedState, storedState)

                val userId = decryptedState.split(":")[0]
                val stateTime = getStateTime(decryptedState)

                validateTime(stateTime)

                deleteState(state)

                Mono.just(userId)
            }
    }

    @Scheduled(fixedRate = 10*60*1000) // This will run the method every 10 minutes
    fun deleteExpiredStates() {
        val currentTime = Instant.now()
        var deletedCount = 0

        stateRepository.findAll()
            .filterWhen { state ->
                val stateTime = Instant.ofEpochMilli(state.timestamp)
                Mono.just(stateTime.plusSeconds(validityPeriodMinutes * 60).isBefore(currentTime))
            }
            .flatMap { state ->
                deletedCount++
                stateRepository.deleteById(state.id)
            }
            .doOnComplete { logger.info("$deletedCount expired states were deleted.") }
            .subscribe()
    }

    private fun validateState(decryptedState: String, storedStateParameter: StateParameter) {
        val parts = decryptedState.split(":")
        if (parts.size != 2 || decryptedState != "${storedStateParameter.userId}:${storedStateParameter.timestamp}") {
            throw InvalidStateParameterException()
        }
    }

    private fun getStateTime(decryptedState: String): Instant {
        val timestamp = decryptedState.split(":")[1].toLongOrNull() ?: throw InvalidStateParameterException()
        return Instant.ofEpochMilli(timestamp)
    }

    private fun validateTime(stateTime: Instant) {
        val currentTime = Instant.now()
        if (currentTime.isAfter(stateTime.plusSeconds(validityPeriodMinutes * 60))) {
            throw InvalidStateParameterException()
        }
    }

    private fun deleteState(state: String) {
        stateRepository.deleteById(state).subscribe()
    }
}