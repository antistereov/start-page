package io.github.antistereov.start.widgets.nextcloud.service

import io.github.antistereov.start.global.model.exception.CannotSaveUserException
import io.github.antistereov.start.global.model.exception.UserNotFoundException
import io.github.antistereov.start.security.AESEncryption
import io.github.antistereov.start.user.model.User
import io.github.antistereov.start.user.repository.UserRepository
import io.github.antistereov.start.widgets.nextcloud.model.NextcloudCalendar
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class CalDavApiService(
    private val userRepository: UserRepository,
    private val aesEncryption: AESEncryption,
    private val eventService: CalDavEventService,
) {

    private val logger = LoggerFactory.getLogger(CalDavApiService::class.java)

    fun addCalendars(userId: String, calendars: List<NextcloudCalendar>): Flux<NextcloudCalendar> {
        logger.debug("Adding calendars for user: $userId.")

        return fetchUser(userId)
            .flatMapMany { user ->
                checkForDuplicates(user, calendars)
                val newCalendars = encryptCalendars(calendars)
                addNewCalendars(user, newCalendars)
            }
    }

    private fun fetchUser(userId: String): Mono<User> {
        logger.debug("Fetching user: $userId.")

        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(UserNotFoundException(userId)))
    }

    private fun checkForDuplicates(user: User, calendars: List<NextcloudCalendar>) {
        logger.debug("Checking for duplicates.")

        val existingIcsLinks = user.nextcloud.calendars.map { it.icsLink }
        val decryptedIcsLinks = existingIcsLinks.map { aesEncryption.decrypt(it) }
        val duplicates = calendars.find { it.icsLink in decryptedIcsLinks }
        if (duplicates != null) {
            throw IllegalArgumentException("Trying to add existing calendars: $duplicates")
        }
    }

    private fun encryptCalendars(calendars: List<NextcloudCalendar>): List<NextcloudCalendar> {
        logger.debug("Encrypting calendars.")

        return calendars.map { calendar ->
            NextcloudCalendar(
                name = aesEncryption.encrypt(calendar.name),
                color = calendar.color,
                icsLink = aesEncryption.encrypt(calendar.icsLink),
                description = calendar.description?.let { aesEncryption.encrypt(it) },
                events = eventService.encryptEvents(calendar.events)
            )
        }
    }

    private fun addNewCalendars(user: User, newCalendars: List<NextcloudCalendar>): Flux<NextcloudCalendar> {
        logger.debug("Adding new calendars.")

        user.nextcloud.calendars.addAll(newCalendars)
        return userRepository.save(user)
            .onErrorMap { throwable ->
                CannotSaveUserException(throwable)
            }
            .flatMapMany { updatedUser ->
                Flux.fromIterable(updatedUser.nextcloud.calendars)
            }
    }

    fun deleteCalendars(userId: String, icsLinks: List<String>?): Flux<NextcloudCalendar> {
        logger.debug("Deleting calendars for user: $userId.")

        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(UserNotFoundException(userId)))
            .flatMapMany { user ->
                val updatedCalendars = if (icsLinks != null) {
                    user.nextcloud.calendars
                        .filter { aesEncryption.decrypt(it.icsLink) !in icsLinks }
                        .toMutableList()
                } else {
                    mutableListOf()
                }
                user.nextcloud.calendars = updatedCalendars
                userRepository.save(user)
                    .onErrorMap { throwable ->
                        CannotSaveUserException(throwable)
                    }
                    .flatMapMany { updatedUser ->
                        Flux.fromIterable(updatedUser.nextcloud.calendars)
                    }
            }
    }

    fun getUserCalendars(userId: String): Mono<List<NextcloudCalendar>> {
        logger.debug("Getting user calendars for user: $userId.")

        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(UserNotFoundException(userId)))
            .map { user ->
                user.nextcloud.calendars.map { calendar ->
                    NextcloudCalendar(
                        name = aesEncryption.decrypt(calendar.name),
                        color = calendar.color,
                        icsLink = aesEncryption.decrypt(calendar.icsLink),
                        description = calendar.description?.let { aesEncryption.decrypt(it) },
                        events = eventService.decryptEvents(calendar.events)
                    )
                }
            }
    }
}