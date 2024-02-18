package io.github.antistereov.start.widgets.nextcloud.service


import io.github.antistereov.start.global.model.exception.CannotSaveUserException
import io.github.antistereov.start.global.model.exception.UserNotFoundException
import io.github.antistereov.start.security.AESEncryption
import io.github.antistereov.start.user.repository.UserRepository
import io.github.antistereov.start.widgets.nextcloud.model.Event
import io.github.antistereov.start.widgets.nextcloud.model.NextcloudCalendar
import io.github.antistereov.start.widgets.nextcloud.model.NextcloudCredentials
import io.github.antistereov.start.widgets.nextcloud.model.RRuleModel
import kotlinx.serialization.modules.EmptySerializersModule
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.stereotype.Service
import org.w3c.dom.Element
import java.time.ZonedDateTime
import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.model.DateTime
import net.fortuna.ical4j.model.Period
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.model.property.RRule
import org.xml.sax.InputSource
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.io.StringReader
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory

@Service
class CalDavService(
    private val nextcloudAuthService: NextcloudAuthService,
    private val userRepository: UserRepository,
    private val aesEncryption: AESEncryption,
) {

    fun getRemoteCalendars(credentials: NextcloudCredentials): List<NextcloudCalendar> {
        val calendarData = fetchCalendarData(credentials)
        return parseCalendarData(calendarData, credentials)
    }

    fun addCalendars(userId: String, calendars: List<NextcloudCalendar>): Flux<NextcloudCalendar> {
        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(UserNotFoundException(userId)))
            .flatMapMany { user ->
                val existingIcsLinks = user.nextcloud.calendars.map { it.icsLink }
                val duplicates = calendars.find { it.icsLink in existingIcsLinks }
                if (duplicates != null) {
                    throw IllegalArgumentException("Trying to add existing calendars: $duplicates")
                }

                val newCalendars = calendars.map { calendar ->
                    NextcloudCalendar(
                        name = aesEncryption.encrypt(calendar.name),
                        color = calendar.color,
                        icsLink = aesEncryption.encrypt(calendar.icsLink),
                        description = calendar.description?.let { aesEncryption.encrypt(it) },
                        events = decryptEvents(calendar.events)
                    )
                }
                user.nextcloud.calendars.addAll(newCalendars)
                userRepository.save(user)
                    .onErrorMap { throwable ->
                        CannotSaveUserException(throwable)
                    }
                    .flatMapMany { updatedUser ->
                        Flux.fromIterable(updatedUser.nextcloud.calendars)
                    }
            }
    }

    fun deleteCalendars(userId: String, icsLinks: List<String>): Flux<NextcloudCalendar> {
        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(UserNotFoundException(userId)))
            .flatMapMany { user ->
                val updatedCalendars = user.nextcloud.calendars
                    .filter { aesEncryption.decrypt(it.icsLink) !in icsLinks }
                    .toMutableList()
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
        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(UserNotFoundException(userId)))
            .map { user ->
                user.nextcloud.calendars.map { calendar ->
                    NextcloudCalendar(
                        name = aesEncryption.decrypt(calendar.name),
                        color = calendar.color,
                        icsLink = aesEncryption.decrypt(calendar.icsLink),
                        description = calendar.description?.let { aesEncryption.decrypt(it) },
                        events = decryptEvents(calendar.events)
                    )
                }
            }
    }

    fun refreshCalendarEvents(userId: String): Flux<NextcloudCalendar> {
        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(UserNotFoundException(userId)))
            .flatMapMany { user ->
                nextcloudAuthService.getCredentials(userId).flatMapMany { credentials ->
                    val calendars = user.nextcloud.calendars
                    Flux.fromIterable(calendars).flatMap { calendar ->
                        val decryptedIcsLink = aesEncryption.decrypt(calendar.icsLink)
                        getCalendarEvents(credentials, decryptedIcsLink)
                            .collectList()
                            .map { encryptEvents(it) }
                            .map { encryptedEvents -> calendar.copy(events = encryptedEvents) }
                    }.collectList()
                }.flatMap { updatedCalendars ->
                    refreshUserCalenders(userId, updatedCalendars)
                }
            }
    }

    private fun refreshUserCalenders(userId: String, calendars: MutableList<NextcloudCalendar>): Flux<NextcloudCalendar> {
        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(UserNotFoundException(userId)))
            .flatMap { user ->
                val updatedCalendars = calendars.map { calendar ->
                    NextcloudCalendar(
                        name = aesEncryption.encrypt(calendar.name),
                        color = calendar.color,
                        icsLink = aesEncryption.encrypt(calendar.icsLink),
                        description = calendar.description?.let { aesEncryption.encrypt(it) },
                        events = mutableListOf()
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
        return Mono.fromCallable {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(icsLink)
                .header("Authorization", Credentials.basic(credentials.username, credentials.password))
                .build()

            val response = client.newCall(request).execute()
            val calendarData = response.body?.string()

            val calendarBuilder = CalendarBuilder()
            val calendar = calendarBuilder.build(calendarData?.let { StringReader(it) })

            val now = ZonedDateTime.now()
            val future = now.plusMonths(1)

            val nowDate = Date.from(now.toInstant())
            val futureDate = Date.from(future.toInstant())

            val period = Period(DateTime(nowDate), DateTime(futureDate))

            val events = calendar.components
                .filterIsInstance<VEvent>()
                .filter { event ->
                    val rruleProperty = event.getProperty(RRule.RRULE) as RRule?

                    if (rruleProperty != null) {
                        // If the event is recurring, check if it occurs within the period
                        val periods = event.calculateRecurrenceSet(period)
                        periods.isNotEmpty()
                    } else {
                        // If the event is not recurring, check if it starts after the current time
                        event.startDate.date.toInstant().atZone(now.zone).toLocalDateTime()
                            .isAfter(now.toLocalDateTime())
                    }
                }
                .map { vEvent ->
                    Event(
                        summary = vEvent.summary.value,
                        description = vEvent.description?.value,
                        location = vEvent.location?.value,
                        start = ZonedDateTime.ofInstant(vEvent.startDate.date.toInstant(), ZoneId.systemDefault()),
                        end = ZonedDateTime.ofInstant(vEvent.endDate.date.toInstant(), ZoneId.systemDefault()),
                        allDay = vEvent.startDate.isUtc,
                        rrule = vEvent.getProperty(RRule.RRULE)?.value?.let { parseRRule(it) }
                    )
                }

            events
        }.flatMapMany { events -> Flux.fromIterable(events) }
    }

    private fun fetchCalendarData(credentials: NextcloudCredentials): String {
        val calendarUrl = "${credentials.url}/remote.php/dav/calendars/${credentials.username}/"
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(calendarUrl)
            .header("Authorization", Credentials.basic(credentials.username, credentials.password))
            .method("PROPFIND", null)
            .build()

        val response = client.newCall(request).execute()
        return response.body?.string() ?: ""
    }

    private fun parseCalendarData(calendarData: String, credentials: NextcloudCredentials): List<NextcloudCalendar> {
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(InputSource(StringReader(calendarData)))
        val calendarElements = document.getElementsByTagName("d:response")

        val calendars = mutableListOf<NextcloudCalendar>()

        for (i in 0 until calendarElements.length) {
            val calendarElement = calendarElements.item(i) as Element
            val isCalendar = checkIsCalendar(calendarElement)

            if (isCalendar) {
                val calendar = createCalendar(calendarElement, credentials)
                if (calendar != null) calendars.add(calendar)
            }
        }

        return calendars
    }

    private fun checkIsCalendar(calendarElement: Element): Boolean {
        val resourceTypeElement = calendarElement.getElementsByTagName("d:resourcetype").item(0) as Element
        return resourceTypeElement.getElementsByTagName("cal:calendar").length > 0
    }

    private fun createCalendar(calendarElement: Element, credentials: NextcloudCredentials): NextcloudCalendar? {
        val hrefElement = calendarElement.getElementsByTagName("d:href").item(0)
        val icsPath = hrefElement.textContent!!
        val icsLink = "${credentials.url}$icsPath?export"

        val nameElement = calendarElement.getElementsByTagName("d:displayname").item(0)
        val colorElement = calendarElement.getElementsByTagName("x1:calendar-color").item(0)
        val descriptionElement = calendarElement.getElementsByTagName("d:calendar-description").item(0)

        val name = nameElement?.textContent
        val color = colorElement?.textContent
        val description = descriptionElement?.textContent

        return if (name != null && color != null && name != "DEFAULT_TASK_CALENDAR_NAME") {
            NextcloudCalendar(
                name,
                color,
                icsLink,
                description,
                mutableListOf()
            )
        } else {
            null
        }
    }

    private fun parseRRule(rruleString: String): RRuleModel {
        val rruleParts = rruleString.split(";").associate {
            val (key, value) = it.split("=")
            key to value
        }

        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmssX")

        return RRuleModel(
            freq = rruleParts["FREQ"],
            until = rruleParts["UNTIL"]?.let { ZonedDateTime.parse(it, formatter) },
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
    
    private fun encryptEvents(events: List<Event>): List<Event> {
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

    private fun decryptEvents(events: List<Event>): List<Event> {
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


}