package io.github.antistereov.start.global.component

import io.github.antistereov.start.global.model.exception.InvalidStateParameterException
import io.github.antistereov.start.security.AESEncryption
import io.github.antistereov.start.global.model.StateParameter
import io.github.antistereov.start.user.repository.StateRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.Instant

@Component
class StateValidation(
    private val aesEncryption: AESEncryption,
    private val stateRepository: StateRepository,
) {

    fun createState(userId: String): Mono<String> {
        val timestamp = Instant.now().toEpochMilli()
        val state = "$userId:$timestamp"
        val encryptedState = aesEncryption.encrypt(state)
        return stateRepository.save(StateParameter(encryptedState, userId, timestamp)).map { encryptedState }
    }

    fun getUserId(state: String, validityPeriodMinutes: Long = 5): Mono<String> {
        val decryptedState = aesEncryption.decrypt(state)
        return stateRepository.findById(state)
            .switchIfEmpty(Mono.error(InvalidStateParameterException()))
            .flatMap { storedState ->
                validateState(decryptedState, storedState)

                val userId = decryptedState.split(":")[0]
                val stateTime = getStateTime(decryptedState)

                validateTime(stateTime, validityPeriodMinutes)

                deleteState(state)

                Mono.just(userId)
            }
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

    private fun validateTime(stateTime: Instant, validityPeriodMinutes: Long) {
        val currentTime = Instant.now()
        if (currentTime.isAfter(stateTime.plusSeconds(validityPeriodMinutes * 60))) {
            throw InvalidStateParameterException()
        }
    }

    private fun deleteState(state: String) {
        stateRepository.deleteById(state).subscribe()
    }
}