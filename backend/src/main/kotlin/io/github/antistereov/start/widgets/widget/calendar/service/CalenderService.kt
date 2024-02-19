package io.github.antistereov.start.widgets.widget.calendar.service

import io.github.antistereov.start.security.AESEncryption
import io.github.antistereov.start.user.model.User
import io.github.antistereov.start.user.service.UserService
import io.github.antistereov.start.widgets.widget.calendar.dto.CalendarDTO
import io.github.antistereov.start.widgets.widget.calendar.model.CalendarType
import io.github.antistereov.start.widgets.widget.calendar.model.OnlineCalendar
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class CalenderService(
    private val userService: UserService,
    private val aesEncryption: AESEncryption,
    private val eventService: EventService,
) {

    private val logger = LoggerFactory.getLogger(CalenderService::class.java)

    fun addCalendars(userId: String, calendars: List<OnlineCalendar>): Flux<OnlineCalendar> {
        logger.debug("Adding calendars for user: $userId.")

        return userService.findById(userId).flatMapMany { user ->
            checkForDuplicates(user, calendars)

            val encryptedCalendars = encryptCalendars(calendars)
            user.widgets.calendar.calendars.addAll(encryptedCalendars)

            userService.save(user).flatMapMany { updatedUser ->
                Flux.fromIterable(updatedUser.widgets.calendar.calendars)
            }
        }
    }

    fun deleteCalendars(userId: String, icsLinks: List<String>): Flux<OnlineCalendar> {
        logger.debug("Deleting calendars for user: $userId.")

        return userService.findById(userId).flatMapMany { user ->
            val updatedCalendars = mutableListOf<OnlineCalendar>()

            if (icsLinks.isNotEmpty()) {
                updatedCalendars.addAll(
                    user.widgets.calendar.calendars.filter { aesEncryption.decrypt(it.icsLink) !in icsLinks }
                )
            }

            user.widgets.calendar.calendars = updatedCalendars

            userService.save(user).flatMapMany { updatedUser ->
                Flux.fromIterable(updatedUser.widgets.calendar.calendars)
            }
        }
    }

    fun getUserCalendars(userId: String): Flux<OnlineCalendar> {
        logger.debug("Getting user calendars for user: $userId.")

        return userService.findById(userId).flatMapMany { user ->
            Flux.fromIterable(user.widgets.calendar.calendars.map { calendar ->
                decryptCalendar(calendar)
            })
        }
    }

    fun getCalendarEvents(userId: String, icsLinks: List<String>): Flux<CalendarDTO> {
        logger.debug("Getting calendar events of calendars: {} for user: {}.", icsLinks, userId)

        return userService.findById(userId).flatMapMany { user ->
            val calendars = mutableListOf<OnlineCalendar>()

            calendars.addAll(
                user.widgets.calendar.calendars
                    .filter { aesEncryption.decrypt(it.icsLink) !in icsLinks }
                    .filter { it.type == CalendarType.Calendar }
            )

            Flux.fromIterable(calendars).flatMap { calendar ->
                eventService.getCalendarEvents(userId, calendar)
            }
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
                auth = calendar.auth,
                type = calendar.type,
            )
        }
    }

    private fun decryptCalendar(calendar: OnlineCalendar): OnlineCalendar {
        logger.debug("Decrypting calendar.")

        return OnlineCalendar(
            name = aesEncryption.decrypt(calendar.name),
            color = calendar.color,
            icsLink = aesEncryption.decrypt(calendar.icsLink),
            description = calendar.description?.let { aesEncryption.decrypt(it) },
            auth = calendar.auth,
            type = calendar.type
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