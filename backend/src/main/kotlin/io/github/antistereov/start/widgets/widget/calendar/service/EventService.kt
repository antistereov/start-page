package io.github.antistereov.start.widgets.widget.calendar.service

import io.github.antistereov.start.security.AESEncryption
import io.github.antistereov.start.widgets.auth.nextcloud.service.NextcloudAuthService
import io.github.antistereov.start.widgets.widget.calendar.dto.CalendarDTO
import io.github.antistereov.start.widgets.widget.calendar.model.CalendarAuth
import io.github.antistereov.start.widgets.widget.calendar.model.CalendarEvent
import io.github.antistereov.start.widgets.widget.calendar.model.OnlineCalendar
import io.github.antistereov.start.widgets.widget.calendar.model.RRuleModel
import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.model.DateTime
import net.fortuna.ical4j.model.Period
import net.fortuna.ical4j.model.component.VEvent
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

    fun getCalendarEvents(userId: String, calendar: OnlineCalendar): Mono<CalendarDTO> {
        return getEvents(userId, calendar)
    }

    fun getEvents(userId: String, calendar: OnlineCalendar): Mono<CalendarDTO> {
        logger.debug("Getting calendar events.")

        return buildWebClient(userId, calendar).flatMap { client ->
            client
                .get()
                .retrieve()
                .bodyToMono(String::class.java)
                .flatMap { calendarData ->
                    Mono.just(CalendarDTO(calendar.icsLink, calendarEvents(calendarData)))
                }
        }
    }

    private fun buildWebClient(userId: String, calendar: OnlineCalendar): Mono<WebClient> {
        val decryptedCalendarIcsLink = aesEncryption.decrypt(calendar.icsLink)
        val client = webClientBuilder
            .baseUrl(decryptedCalendarIcsLink)
        return when (calendar.auth) {
            CalendarAuth.None -> Mono.just(client.build())
            CalendarAuth.Nextcloud -> {
                nextcloudAuthService.getCredentials(userId).map { credentials ->
                    client.defaultHeader("Authorization", Credentials.basic(credentials.username, credentials.password))
                        .build()
                }
            }
        }
    }

    fun calendarEvents(calendarData: String): List<CalendarEvent> {
        val calendarBuilder = CalendarBuilder()
        val calendar = calendarBuilder.build(StringReader(calendarData))

        val now = ZonedDateTime.now()
        val future = now.plusYears(1)

        val nowDate = Date.from(now.toInstant())
        val futureDate = Date.from(future.toInstant())

        val period = Period(DateTime(nowDate), DateTime(futureDate))

        return calendar.components
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
                    rrule = vEvent.getProperty(RRule.RRULE)?.value?.let { parseRRule(it) }
                )
            }
    }

    fun parseRRule(rruleString: String): RRuleModel {
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