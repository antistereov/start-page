package io.github.antistereov.start.widgets.nextcloud.service

import io.github.antistereov.start.global.model.exception.CannotSaveUserException
import io.github.antistereov.start.global.model.exception.UserNotFoundException
import io.github.antistereov.start.security.AESEncryption
import io.github.antistereov.start.user.repository.UserRepository
import io.github.antistereov.start.widgets.nextcloud.model.Event
import io.github.antistereov.start.widgets.nextcloud.model.NextcloudCalendar
import io.github.antistereov.start.widgets.nextcloud.model.NextcloudCredentials
import io.github.antistereov.start.widgets.nextcloud.model.RRuleModel
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
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class CalDavEventService(
    private val nextcloudAuthService: NextcloudAuthService,
    private val userRepository: UserRepository,
    private val aesEncryption: AESEncryption,
) {

    private val logger = LoggerFactory.getLogger(CalDavEventService::class.java)

    fun refreshCalendarEvents(userId: String): Flux<NextcloudCalendar> {
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
                                calendar.events = events
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
        calendars: MutableList<NextcloudCalendar>
    ): Flux<NextcloudCalendar> {
        logger.debug("Refreshing user calendars.")

        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(UserNotFoundException(userId)))
            .flatMap { user ->
                val updatedCalendars = calendars.map { calendar ->
                    NextcloudCalendar(
                        name = calendar.name,
                        color = calendar.color,
                        icsLink = calendar.icsLink,
                        description = calendar.description,
                        events = encryptEvents(calendar.events)
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

    private fun getCalendarEvents(credentials: NextcloudCredentials, icsLink: String): Flux<Event> {
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

                val events = calendar.components
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
                        Event(
                            summary = vEvent.summary.value,
                            description = vEvent.description?.value,
                            location = vEvent.location?.value,
                            start = vEvent.startDate.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),
                            end = vEvent.endDate.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),
                            allDay = vEvent.startDate.isUtc,
                            rrule = vEvent.getProperty(RRule.RRULE)?.value?.let { parseRRule(it) }
                        )
                    }
                Flux.fromIterable(events)
            }
    }


    fun encryptEvents(events: List<Event>): List<Event> {
        logger.debug("Encrypting events.")

        return events.map { event ->
            Event(
                summary = aesEncryption.encrypt(event.summary),
                description = event.description?.let { aesEncryption.encrypt(it) },
                location = event.location?.let { aesEncryption.encrypt(it) },
                start = event.start,
                end = event.end,
                allDay = event.allDay,
                rrule = event.rrule
            )
        }
    }

    fun decryptEvents(events: List<Event>): List<Event> {
        logger.debug("Decrypting events.")

        return events.map { event ->
            Event(
                summary = aesEncryption.decrypt(event.summary),
                description = event.description?.let { aesEncryption.decrypt(it) },
                location = event.location?.let { aesEncryption.decrypt(it) },
                start = event.start,
                end = event.end,
                allDay = event.allDay,
                rrule = event.rrule
            )
        }
    }

    private fun parseRRule(rruleString: String): RRuleModel {
        logger.debug("Parsing RRule.")

        val rruleParts = rruleString.split(";").associate {
            val (key, value) = it.split("=")
            key to value
        }

        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmssX")

        return RRuleModel(
            freq = rruleParts["FREQ"],
            until = rruleParts["UNTIL"]?.let { LocalDateTime.parse(it, formatter) },
            count = rruleParts["COUNT"]?.toInt(),
            interval = rruleParts["INTERVAL"]?.toInt(),
            byDay = rruleParts["BYDAY"]?.split(","),
            byMonthDay = rruleParts["BYMONTHDAY"]?.split(",")?.map { it.toInt() },
            byYearDay = rruleParts["BYYEARDAY"]?.split(",")?.map { it.toInt() },
            byWeekNo = rruleParts["BYWEEKNO"]?.split(",")?.map { it.toInt() },
            byMonth = rruleParts["BYMONTH"]?.split(",")?.map { it.toInt() },
            bySetPos = rruleParts["BYSETPOS"]?.split(",")?.map { it.toInt() },
            wkst = rruleParts["WKST"]
        )
    }
}