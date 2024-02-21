package io.github.antistereov.start.widgets.widget.calendar.service

import io.github.antistereov.start.security.AESEncryption
import io.github.antistereov.start.user.model.User
import io.github.antistereov.start.user.service.UserService
import io.github.antistereov.start.widgets.widget.calendar.model.CalendarType
import io.github.antistereov.start.widgets.widget.calendar.model.OnlineCalendar
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant

@Service
class CalenderService(
    private val userService: UserService,
    private val aesEncryption: AESEncryption,
    private val eventService: EventService,
) {

    private val logger = LoggerFactory.getLogger(CalenderService::class.java)

    fun addCalendars(userId: String, calendars: List<OnlineCalendar>): Mono<List<OnlineCalendar>> {
        logger.debug("Adding calendars for user: $userId.")

        return userService.findById(userId).flatMap { user ->
            checkForDuplicates(user, calendars)

            val encryptedCalendars = calendars.map { encryptCalendar(it) }
            user.widgets.calendar.calendars.addAll(encryptedCalendars)

            userService.save(user).map { calendars }
        }
    }

    fun deleteCalendars(userId: String, icsLinks: List<String>): Mono<List<OnlineCalendar>> {
        logger.debug("Deleting calendars for user: $userId.")

        return userService.findById(userId).flatMap { user ->
            val updatedCalendars = mutableListOf<OnlineCalendar>()

            if (icsLinks.isNotEmpty()) {
                updatedCalendars.addAll(
                    user.widgets.calendar.calendars.filter { aesEncryption.decrypt(it.icsLink) !in icsLinks }
                )
            }

            user.widgets.calendar.calendars = updatedCalendars

            userService.save(user).map { updatedUser ->
                updatedUser.widgets.calendar.calendars
            }
        }
    }

    fun getUserCalendars(userId: String): Mono<List<OnlineCalendar>> {
        logger.debug("Getting user calendars for user: $userId.")

        return userService.findById(userId).map { user ->
            user.widgets.calendar.calendars.map { calendar ->
                decryptCalendar(calendar)
            }
        }
    }

    fun updateCalendarEvents(userId: String, icsLinks: List<String>): Flux<OnlineCalendar> {
        logger.debug("Getting calendar events of calendars: {} for user: {}.", icsLinks.ifEmpty { "all" }, userId)

        return userService.findById(userId).flatMapIterable { user ->
            val calendars = user.widgets.calendar.calendars
                .filter { it.type == CalendarType.Calendar }
                .map { decryptCalendar(it) }.toMutableList()
            if (icsLinks.isNotEmpty()) calendars.removeIf { it.icsLink !in icsLinks }

            calendars
        }.flatMap { calendar ->
            eventService.updateCalenderEvents(userId, calendar).flatMap { updatedCalendar ->
                updatedCalendar.apply { lastUpdated = Instant.now() }
                saveUpdatedCalendar(userId, updatedCalendar)
            }
        }
    }

    private fun saveUpdatedCalendar(userId: String, calendar: OnlineCalendar): Mono<OnlineCalendar> {
        logger.debug("Saving updated calendar: ${calendar.name} for user: $userId.")

        return userService.findById(userId).flatMap { user ->
            user.widgets.calendar.calendars = user.widgets.calendar.calendars.map {
                if (aesEncryption.decrypt(it.icsLink) == calendar.icsLink) encryptCalendar(calendar) else it
            }.toMutableList()
            userService.save(user).map { calendar }
        }
    }

    private fun encryptCalendar(calendar: OnlineCalendar): OnlineCalendar {
        logger.debug("Encrypting calendars.")

        return OnlineCalendar(
                name = aesEncryption.encrypt(calendar.name),
                color = calendar.color,
                icsLink = aesEncryption.encrypt(calendar.icsLink),
                description = calendar.description?.let { aesEncryption.encrypt(it) },
                auth = calendar.auth,
                type = calendar.type,
                lastUpdated = calendar.lastUpdated,
                timezone = calendar.timezone,
                readOnly = calendar.readOnly,
                events = eventService.encryptEvents(calendar.events)
            )
    }

    private fun decryptCalendar(calendar: OnlineCalendar): OnlineCalendar {
        logger.debug("Decrypting calendar.")

        return OnlineCalendar(
            name = aesEncryption.decrypt(calendar.name),
            color = calendar.color,
            icsLink = aesEncryption.decrypt(calendar.icsLink),
            description = calendar.description?.let { aesEncryption.decrypt(it) },
            auth = calendar.auth,
            type = calendar.type,
            lastUpdated = calendar.lastUpdated,
            timezone = calendar.timezone,
            readOnly = calendar.readOnly,
            events = eventService.decryptEvents(calendar.events)
        )
    }

    private fun checkForDuplicates(user: User, calendars: List<OnlineCalendar>) {
        logger.debug("Checking for duplicates.")

        val existingIcsLinks = user.widgets.calendar.calendars.map { it.icsLink }
        val decryptedIcsLinks = existingIcsLinks.map { aesEncryption.decrypt(it) }
        val duplicates = calendars.find { it.icsLink in decryptedIcsLinks }
        if (duplicates != null) {
            throw IllegalArgumentException("Trying to add existing calendars: $duplicates")
        }
    }
}