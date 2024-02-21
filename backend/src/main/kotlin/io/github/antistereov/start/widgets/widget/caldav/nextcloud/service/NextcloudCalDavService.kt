package io.github.antistereov.start.widgets.widget.caldav.nextcloud.service


import io.github.antistereov.start.widgets.widget.caldav.calendar.model.CalDavCalendar
import io.github.antistereov.start.widgets.auth.nextcloud.model.NextcloudCredentials
import io.github.antistereov.start.widgets.widget.caldav.base.model.CalDavAuthType
import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.data.ParserException
import net.fortuna.ical4j.model.Property
import net.fortuna.ical4j.model.Component
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.model.component.VToDo
import net.fortuna.ical4j.model.property.Description
import net.fortuna.ical4j.model.property.XProperty
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
import java.io.IOException
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

@Service
class NextcloudCalDavService {

    private val logger = LoggerFactory.getLogger(NextcloudCalDavService::class.java)

    fun getRemoteCalendars(credentials: NextcloudCredentials): Flux<CalDavCalendar> {
        logger.debug("Getting remote calendars for user: ${credentials.username}.")

        return getRemoteCalendarsRaw(credentials).flatMapMany { calendarData ->
                parseCalendarData(calendarData)
            }
    }

    fun getRemoteCalendarsRaw(credentials: NextcloudCredentials): Mono<MutableMap<String, String>> {
        return Mono.fromCallable {
            val calendarUrl = "${credentials.host}/remote.php/dav/calendars/${credentials.username}/"
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(calendarUrl)
                .header("Authorization", Credentials.basic(credentials.username, credentials.password))
                .method("PROPFIND", null)
                .build()

            val response = client.newCall(request).execute()
            val calendarData = response.body?.string() ?: ""
            val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(InputSource(StringReader(calendarData)))
            val calendarElements = document.getElementsByTagName("d:response")

            val calendarDataMap = mutableMapOf<String, String>()

            for (i in 0 until calendarElements.length) {
                val calendarElement = calendarElements.item(i) as Element
                val hrefElement = calendarElement.getElementsByTagName("d:href").item(0)
                val icsPath = hrefElement.textContent!!
                val icsLink = "${credentials.host}$icsPath?export"

                // Fetch the raw calendar data
                val calendarRequest = Request.Builder()
                    .url(icsLink)
                    .header("Authorization", Credentials.basic(credentials.username, credentials.password))
                    .build()

                val calendarResponse = client.newCall(calendarRequest).execute()
                val rawCalendarData = calendarResponse.body?.string() ?: ""

                calendarDataMap[icsLink] = rawCalendarData
            }

            calendarDataMap
        }.subscribeOn(Schedulers.boundedElastic())
    }

    private fun parseCalendarData(calendarDataMap: Map<String, String>): Flux<CalDavCalendar> {
        logger.debug("Parsing calendar data.")

        return Mono.fromCallable {
            val calendars = mutableListOf<CalDavCalendar>()

            for ((icsLink, rawCalendarData) in calendarDataMap) {
                val isCalendar = isCalendar(rawCalendarData)
                val isTasks = isTaskList(rawCalendarData)

                if (isCalendar) {
                    val calendar = createCalendar(icsLink, rawCalendarData)
                    if (calendar != null) calendars.add(calendar)
                }

                if (isTasks) {
                    val calendar = createCalendar(icsLink, rawCalendarData)
                    if (calendar != null) calendars.add(calendar)
                }
            }

            calendars
        }.subscribeOn(Schedulers.boundedElastic())
            .flatMapMany { calendars -> Flux.fromIterable(calendars) }
    }

    private fun isTaskList(icsData: String): Boolean {
        try {
            val builder = CalendarBuilder()
            val calendar = builder.build(StringReader(icsData))
            return calendar.getComponents<VToDo>(Component.VTODO).isNotEmpty()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: ParserException) {
            logger.warn("Skipping calendar: ${e.message}")
        }

        return false
    }

    private fun isCalendar(icsData: String): Boolean {
        try {
            val builder = CalendarBuilder()
            val calendar = builder.build(StringReader(icsData))
            return calendar.getComponents<VEvent>(Component.VEVENT).isNotEmpty()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: ParserException) {
            logger.warn("Skipping calendar: ${e.message}")
        }

        return false
    }

    private fun createCalendar(icsLink: String, rawCalendarData: String): CalDavCalendar? {
        logger.debug("Creating calendar entity.")

        val builder = CalendarBuilder()
        val calendar = builder.build(StringReader(rawCalendarData))

        val name = calendar.getProperty<XProperty>("X-WR-CALNAME")?.value
        val color = calendar.getProperty<XProperty>("X-APPLE-CALENDAR-COLOR")?.value
        val description = calendar.getProperty<Description>(Property.DESCRIPTION)?.value

        return if (name != null && color != null) {
            CalDavCalendar(
                name,
                color,
                icsLink,
                description,
                CalDavAuthType.Nextcloud,
                null,
                false,
            )
        } else {
            null
        }
    }
}