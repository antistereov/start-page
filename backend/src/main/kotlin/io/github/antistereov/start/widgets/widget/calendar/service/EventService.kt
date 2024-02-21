package io.github.antistereov.start.widgets.widget.calendar.service

import io.github.antistereov.start.security.AESEncryption
import io.github.antistereov.start.widgets.auth.nextcloud.service.NextcloudAuthService
import io.github.antistereov.start.widgets.widget.calendar.model.CalendarAuth
import io.github.antistereov.start.widgets.widget.calendar.model.CalendarEvent
import io.github.antistereov.start.widgets.widget.calendar.model.OnlineCalendar
import io.github.antistereov.start.widgets.widget.calendar.model.RRuleModel
import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.model.Period
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.model.parameter.Value
import net.fortuna.ical4j.model.property.RRule
import okhttp3.Credentials
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.io.StringReader
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class EventService(
    private val nextcloudAuthService: NextcloudAuthService,
    private val webClientBuilder: WebClient.Builder,
    private val aesEncryption: AESEncryption,
) {

    private val logger = LoggerFactory.getLogger(EventService::class.java)

    fun updateCalenderEvents(userId: String, calendar: OnlineCalendar): Mono<OnlineCalendar> {
        logger.debug("Updating calendar events for calendar: ${calendar.name} for user: $userId")

        return getEvents(userId, calendar)
    }

    private fun getEvents(userId: String, calendar: OnlineCalendar): Mono<OnlineCalendar> {
        logger.debug("Getting calendar events for calendar: ${calendar.name} for user: $userId.")

        return buildWebClient(userId, calendar).flatMap { client ->
            client
                .get()
                .retrieve()
                .bodyToMono(String::class.java)
                .map { calendarData ->
                    updateCalendar(calendar, getCalendarEvents(calendarData))
                }
        }
    }

    private fun buildWebClient(userId: String, calendar: OnlineCalendar): Mono<WebClient> {
        logger.debug("Building web client for calendar: ${calendar.name} for user: $userId.")

        return when (calendar.auth) {
            CalendarAuth.None -> Mono.just(webClientBuilder.clone().baseUrl(calendar.icsLink).build())
            CalendarAuth.Nextcloud -> {
                nextcloudAuthService.getCredentials(userId).map { credentials ->
                    webClientBuilder.clone()
                        .baseUrl(calendar.icsLink)
                        .defaultHeader("Authorization", Credentials.basic(credentials.username, credentials.password))
                        .build()
                }
            }
        }
    }

    private fun updateCalendar(calendar: OnlineCalendar, calendarEvents: List<CalendarEvent>): OnlineCalendar {
        logger.debug("Updating calendar.")
        return calendar.apply {
            this.events = calendarEvents
        }
    }

    private fun getCalendarEvents(calendarData: String): List<CalendarEvent> {
        logger.debug("Getting calendar events.")

        val calendarBuilder = CalendarBuilder()
        val calendar = calendarBuilder.build(StringReader(calendarData))

        val now = ZonedDateTime.now()
        val future = now.plusYears(1)

        val nowDate = net.fortuna.ical4j.model.DateTime(Date.from(now.toInstant()))
        val futureDate = net.fortuna.ical4j.model.DateTime(Date.from(future.toInstant()))

        val period = Period(nowDate, futureDate)

        return calendar.components
            .filterIsInstance<VEvent>()
            .filter { event ->
                val rruleProperty = event.getProperty<RRule>(RRule.RRULE)

                if (rruleProperty != null) {
                    val seedDate = net.fortuna.ical4j.model.DateTime(event.startDate.date)
                    val periods = rruleProperty.recur.getDates(seedDate, period, Value.DATE_TIME)
                    periods.isNotEmpty()
                } else {
                    event.startDate.date.toInstant().atZone(now.zone).isAfter(now)
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
                    rrule = vEvent.getProperty<RRule>(RRule.RRULE)?.value?.let { parseRRule(it) }
                )
            }
    }

    fun parseRRule(rruleString: String): RRuleModel? {
        logger.debug("Parsing RRule.")

        val rruleParts = rruleString.split(";").associate {
            val (key, value) = it.split("=")
            key to value
        }

        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmssX")

        return try {
            RRuleModel(
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
        } catch (e: Exception) {
            logger.error("Error parsing RRULE string: $rruleString", e)
            null
        }
    }

    fun encryptEvents(events: List<CalendarEvent>): List<CalendarEvent> {
        logger.debug("Encrypting events.")

        return events.map { event ->
            event.copy(
                summary = aesEncryption.encrypt(event.summary),
                description = event.description?.let { aesEncryption.encrypt(it) },
                location = event.location?.let { aesEncryption.encrypt(it) },
            )
        }
    }

    fun decryptEvents(events: List<CalendarEvent>): List<CalendarEvent> {
        logger.debug("Decrypting events.")

        return events.map { event ->
            event.copy(
                summary = aesEncryption.decrypt(event.summary),
                description = event.description?.let { aesEncryption.decrypt(it) },
                location = event.location?.let { aesEncryption.decrypt(it) },
            )
        }
    }
}