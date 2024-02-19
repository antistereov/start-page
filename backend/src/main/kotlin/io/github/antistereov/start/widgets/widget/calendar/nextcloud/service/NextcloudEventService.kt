package io.github.antistereov.start.widgets.widget.calendar.nextcloud.service

import io.github.antistereov.start.global.model.exception.CannotSaveUserException
import io.github.antistereov.start.global.model.exception.UserNotFoundException
import io.github.antistereov.start.security.AESEncryption
import io.github.antistereov.start.user.repository.UserRepository
import io.github.antistereov.start.widgets.widget.calendar.model.CalendarEvent
import io.github.antistereov.start.widgets.widget.calendar.model.OnlineCalendar
import io.github.antistereov.start.widgets.auth.nextcloud.model.NextcloudCredentials
import io.github.antistereov.start.widgets.auth.nextcloud.service.NextcloudAuthService
import io.github.antistereov.start.widgets.widget.calendar.caldav.service.CalDavEventService
import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.model.DateTime
import net.fortuna.ical4j.model.Period
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.model.property.RRule
import okhttp3.Credentials
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.io.StringReader
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

@Service
class NextcloudEventService(
    private val nextcloudAuthService: NextcloudAuthService,
    private val userRepository: UserRepository,
    private val aesEncryption: AESEncryption,
    private val calDavEventService: CalDavEventService,
) {

    private val logger = LoggerFactory.getLogger(NextcloudEventService::class.java)

    fun refreshCalendarEvents(userId: String): Flux<OnlineCalendar> {
        logger.debug("Refreshing calendar events for user: $userId.")

        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(UserNotFoundException(userId)))
            .flatMapMany { user ->
                nextcloudAuthService.getCredentials(userId).flatMapMany { credentials ->
                    val calendars = user.nextcloud.calendars
                    Flux.fromIterable(calendars).flatMap { calendar ->
                        val decryptedIcsLink = aesEncryption.decrypt(calendar.icsLink)
                        getCalendarEvents(credentials, decryptedIcsLink)
                            .collectList()
                            .map { events ->
                                calendar.calendarEvents = events
                                calendar
                            }
                    }.collectList()
                }.flatMap { updatedCalendars ->
                    refreshUserCalenders(userId, updatedCalendars)
                }
            }
    }

    private fun refreshUserCalenders(
        userId: String,
        calendars: MutableList<OnlineCalendar>
    ): Flux<OnlineCalendar> {
        logger.debug("Refreshing user calendars.")

        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(UserNotFoundException(userId)))
            .flatMap { user ->
                val updatedCalendars = calendars.map { calendar ->
                    OnlineCalendar(
                        name = calendar.name,
                        color = calendar.color,
                        icsLink = calendar.icsLink,
                        description = calendar.description,
                        calendarEvents = calDavEventService.encryptEvents(calendar.calendarEvents)
                    )
                }.toMutableList()
                user.nextcloud.calendars = updatedCalendars
                userRepository.save(user)
                    .onErrorMap { throwable ->
                        CannotSaveUserException(throwable)
                    }
            }.flatMapMany { updatedUser ->
                Flux.fromIterable(updatedUser.nextcloud.calendars)
            }
    }

    private fun getCalendarEvents(credentials: NextcloudCredentials, icsLink: String): Flux<CalendarEvent> {
        logger.debug("Getting calendar events.")

        val client = WebClient.builder()
            .baseUrl(icsLink)
            .defaultHeader("Authorization", Credentials.basic(credentials.username, credentials.password))
            .build()
        return client.get()
            .retrieve()
            .bodyToMono(String::class.java)
            .flatMapMany { calendarData ->

                val calendarBuilder = CalendarBuilder()
                val calendar = calendarBuilder.build(calendarData?.let { StringReader(it) })

                val now = ZonedDateTime.now()
                val future = now.plusYears(1)

                val nowDate = Date.from(now.toInstant())
                val futureDate = Date.from(future.toInstant())

                val period = Period(DateTime(nowDate), DateTime(futureDate))

                val calendarEvents = calendar.components
                    .filterIsInstance<VEvent>()
                    .filter { event ->
                        val rruleProperty = event.getProperty(RRule.RRULE) as RRule?

                        if (rruleProperty != null) {
                            val periods = event.calculateRecurrenceSet(period)
                            periods.isNotEmpty()
                        } else {
                            event.startDate.date.toInstant().atZone(now.zone).toLocalDateTime()
                                .isAfter(now.toLocalDateTime())
                        }
                    }
                    .map { vEvent ->
                        CalendarEvent(
                            summary = vEvent.summary.value,
                            description = vEvent.description?.value,
                            location = vEvent.location?.value,
                            start = vEvent.startDate.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),
                            end = vEvent.endDate.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),
                            allDay = vEvent.startDate.isUtc,
                            rrule = vEvent.getProperty(RRule.RRULE)?.value?.let { calDavEventService.parseRRule(it) }
                        )
                    }
                Flux.fromIterable(calendarEvents)
            }
    }



}