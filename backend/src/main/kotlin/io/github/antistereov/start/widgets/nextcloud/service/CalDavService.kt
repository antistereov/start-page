package io.github.antistereov.start.widgets.nextcloud.service


import io.github.antistereov.start.global.model.exception.CannotSaveUserException
import io.github.antistereov.start.global.model.exception.UserNotFoundException
import io.github.antistereov.start.security.AESEncryption
import io.github.antistereov.start.user.repository.UserRepository
import io.github.antistereov.start.widgets.nextcloud.model.Event
import io.github.antistereov.start.widgets.nextcloud.model.NextcloudCalendar
import io.github.antistereov.start.widgets.nextcloud.model.NextcloudCredentials
import io.github.antistereov.start.widgets.nextcloud.model.RRuleModel
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
import org.springframework.web.reactive.function.client.WebClient
import org.xml.sax.InputSource
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.io.StringReader
import java.time.LocalDateTime
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

    fun getRemoteCalendars(credentials: NextcloudCredentials): Flux<NextcloudCalendar> {
        return fetchCalendarData(credentials).flatMapMany { calendarData ->
            parseCalendarData(calendarData, credentials)
        }
    }

    fun addCalendars(userId: String, calendars: List<NextcloudCalendar>): Flux<NextcloudCalendar> {
        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(UserNotFoundException(userId)))
            .flatMapMany { user ->
                val existingIcsLinks = user.nextcloud.calendars.map { it.icsLink }
                val decryptedIcsLinks = existingIcsLinks.map { aesEncryption.decrypt(it) }
                val duplicates = calendars.find { it.icsLink in decryptedIcsLinks }
                if (duplicates != null) {
                    throw IllegalArgumentException("Trying to add existing calendars: $duplicates")
                }

                val newCalendars = calendars.map { calendar ->
                    NextcloudCalendar(
                        name = aesEncryption.encrypt(calendar.name),
                        color = calendar.color,
                        icsLink = aesEncryption.encrypt(calendar.icsLink),
                        description = calendar.description?.let { aesEncryption.encrypt(it) },
                        events = encryptEvents(calendar.events)
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

    fun deleteCalendars(userId: String, icsLinks: List<String>?): Flux<NextcloudCalendar> {
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

    private fun fetchCalendarData(credentials: NextcloudCredentials): Mono<String> {
        return Mono.fromCallable {
            val calendarUrl = "${credentials.url}/remote.php/dav/calendars/${credentials.username}/"
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(calendarUrl)
                .header("Authorization", Credentials.basic(credentials.username, credentials.password))
                .method("PROPFIND", null)
                .build()

            val response = client.newCall(request).execute()
            response.body?.string() ?: ""
        }.subscribeOn(Schedulers.boundedElastic())
    }

    private fun parseCalendarData(calendarData: String, credentials: NextcloudCredentials): Flux<NextcloudCalendar> {
        return Mono.fromCallable {
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

            calendars
        }.subscribeOn(Schedulers.boundedElastic())
            .flatMapMany { calendars -> Flux.fromIterable(calendars) }
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