package io.github.antistereov.start.widgets.widget.calendar.service

import io.github.antistereov.start.global.model.exception.CannotSaveUserException
import io.github.antistereov.start.global.model.exception.UserNotFoundException
import io.github.antistereov.start.security.AESEncryption
import io.github.antistereov.start.user.model.User
import io.github.antistereov.start.user.repository.UserRepository
import io.github.antistereov.start.widgets.widget.calendar.model.OnlineCalendar
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.w3c.dom.Element
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class CalDavCalenderService(
    private val userRepository: UserRepository,
    private val aesEncryption: AESEncryption,
    private val eventService: CalDavEventService,
) {

    private val logger = LoggerFactory.getLogger(CalDavCalenderService::class.java)

    fun addCalendars(userId: String, calendars: List<OnlineCalendar>): Flux<OnlineCalendar> {
        logger.debug("Adding calendars for user: $userId.")

        return fetchUser(userId)
            .flatMapMany { user ->
                checkForDuplicates(user, calendars)
                val newCalendars = encryptCalendars(calendars)
                addNewCalendars(user, newCalendars)
            }
    }

    fun deleteCalendars(userId: String, icsLinks: List<String>?): Flux<OnlineCalendar> {
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

    fun getUserCalendars(userId: String): Mono<List<OnlineCalendar>> {
        logger.debug("Getting user calendars for user: $userId.")

        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(UserNotFoundException(userId)))
            .map { user ->
                user.nextcloud.calendars.map { calendar ->
                    OnlineCalendar(
                        name = aesEncryption.decrypt(calendar.name),
                        color = calendar.color,
                        icsLink = aesEncryption.decrypt(calendar.icsLink),
                        description = calendar.description?.let { aesEncryption.decrypt(it) },
                        calendarEvents = eventService.decryptEvents(calendar.calendarEvents)
                    )
                }
            }
    }
    fun checkIsCalendar(calendarElement: Element): Boolean {
        logger.debug("Checking if element is calendar.")

        val resourceTypeElement = calendarElement.getElementsByTagName("d:resourcetype").item(0) as Element
        return resourceTypeElement.getElementsByTagName("cal:calendar").length > 0
    }

    private fun fetchUser(userId: String): Mono<User> {
        logger.debug("Fetching user: $userId.")

        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(UserNotFoundException(userId)))
    }

    private fun checkForDuplicates(user: User, calendars: List<OnlineCalendar>) {
        logger.debug("Checking for duplicates.")

        val existingIcsLinks = user.nextcloud.calendars.map { it.icsLink }
        val decryptedIcsLinks = existingIcsLinks.map { aesEncryption.decrypt(it) }
        val duplicates = calendars.find { it.icsLink in decryptedIcsLinks }
        if (duplicates != null) {
            throw IllegalArgumentException("Trying to add existing calendars: $duplicates")
        }
    }

    private fun encryptCalendars(calendars: List<OnlineCalendar>): List<OnlineCalendar> {
        logger.debug("Encrypting calendars.")

        return calendars.map { calendar ->
            OnlineCalendar(
                name = aesEncryption.encrypt(calendar.name),
                color = calendar.color,
                icsLink = aesEncryption.encrypt(calendar.icsLink),
                description = calendar.description?.let { aesEncryption.encrypt(it) },
                calendarEvents = eventService.encryptEvents(calendar.calendarEvents)
            )
        }
    }

    private fun addNewCalendars(user: User, newCalendars: List<OnlineCalendar>): Flux<OnlineCalendar> {
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
}