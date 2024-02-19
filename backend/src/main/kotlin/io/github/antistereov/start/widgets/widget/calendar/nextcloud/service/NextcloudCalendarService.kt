package io.github.antistereov.start.widgets.widget.calendar.nextcloud.service


import io.github.antistereov.start.widgets.widget.calendar.model.OnlineCalendar
import io.github.antistereov.start.widgets.auth.nextcloud.model.NextcloudCredentials
import io.github.antistereov.start.widgets.widget.calendar.caldav.service.CalDavCalenderService
import io.github.antistereov.start.widgets.widget.calendar.model.CalendarAuth
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.w3c.dom.Element
import org.xml.sax.InputSource
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

@Service
class NextcloudCalendarService(
    private val calendarService: CalDavCalenderService,
) {

    private val logger = LoggerFactory.getLogger(NextcloudCalendarService::class.java)

    fun getRemoteCalendars(credentials: NextcloudCredentials): Flux<OnlineCalendar> {
        logger.debug("Getting remote calendars for user: ${credentials.username}.")

        return fetchCalendarData(credentials).flatMapMany { calendarData ->
            parseCalendarData(calendarData, credentials)
        }
    }

    private fun fetchCalendarData(credentials: NextcloudCredentials): Mono<String> {
        logger.debug("Fetching calendar data.")

        return Mono.fromCallable {
            val calendarUrl = "${credentials.host}/remote.php/dav/calendars/${credentials.username}/"
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(calendarUrl)
                .header("Authorization", Credentials.basic(credentials.username!!, credentials.password!!))
                .method("PROPFIND", null)
                .build()

            val response = client.newCall(request).execute()
            response.body?.string() ?: ""
        }.subscribeOn(Schedulers.boundedElastic())
    }

    private fun parseCalendarData(calendarData: String, credentials: NextcloudCredentials): Flux<OnlineCalendar> {
        logger.debug("Parsing calendar data.")

        return Mono.fromCallable {
            val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(InputSource(StringReader(calendarData)))
            val calendarElements = document.getElementsByTagName("d:response")

            val calendars = mutableListOf<OnlineCalendar>()

            for (i in 0 until calendarElements.length) {
                val calendarElement = calendarElements.item(i) as Element
                val isCalendar = calendarService.checkIsCalendar(calendarElement)

                if (isCalendar) {
                    val calendar = createCalendar(calendarElement, credentials)
                    if (calendar != null) calendars.add(calendar)
                }
            }

            calendars
        }.subscribeOn(Schedulers.boundedElastic())
            .flatMapMany { calendars -> Flux.fromIterable(calendars) }
    }

    private fun createCalendar(calendarElement: Element, credentials: NextcloudCredentials): OnlineCalendar? {
        logger.debug("Creating calendar entity.")

        val hrefElement = calendarElement.getElementsByTagName("d:href").item(0)
        val icsPath = hrefElement.textContent!!
        val icsLink = "${credentials.host}$icsPath?export"

        val nameElement = calendarElement.getElementsByTagName("d:displayname").item(0)
        val colorElement = calendarElement.getElementsByTagName("x1:calendar-color").item(0)
        val descriptionElement = calendarElement.getElementsByTagName("d:calendar-description").item(0)

        val name = nameElement?.textContent
        val color = colorElement?.textContent
        val description = descriptionElement?.textContent

        return if (name != null && color != null && name != "DEFAULT_TASK_CALENDAR_NAME") {
            OnlineCalendar(
                name,
                color,
                icsLink,
                CalendarAuth.Nextcloud,
                description,
                mutableListOf()
            )
        } else {
            null
        }
    }


}