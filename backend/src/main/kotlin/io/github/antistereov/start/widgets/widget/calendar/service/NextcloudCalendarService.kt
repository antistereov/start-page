package io.github.antistereov.start.widgets.widget.calendar.service


import io.github.antistereov.start.widgets.widget.calendar.model.OnlineCalendar
import io.github.antistereov.start.widgets.auth.nextcloud.model.NextcloudCredentials
import io.github.antistereov.start.widgets.widget.calendar.model.CalendarAuth
import io.github.antistereov.start.widgets.widget.calendar.model.CalendarType
import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.data.ParserException
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
class NextcloudCalendarService {

    private val logger = LoggerFactory.getLogger(NextcloudCalendarService::class.java)

    fun getRemoteCalendars(credentials: NextcloudCredentials): Flux<OnlineCalendar> {
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

    private fun parseCalendarData(calendarDataMap: Map<String, String>): Flux<OnlineCalendar> {
        logger.debug("Parsing calendar data.")

        return Mono.fromCallable {
            val calendars = mutableListOf<OnlineCalendar>()

            for ((icsLink, rawCalendarData) in calendarDataMap) {
                val isCalendar = isCalendar(rawCalendarData)
                val isTasks = isTaskList(rawCalendarData)

                if (isCalendar) {
                    val calendar = createCalendar(icsLink, rawCalendarData, CalendarType.Calendar)
                    if (calendar != null) calendars.add(calendar)
                }

                if (isTasks) {
                    val calendar = createCalendar(icsLink, rawCalendarData, CalendarType.Tasks)
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
            return !calendar.getComponents("VTODO").isEmpty()
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
            return !calendar.getComponents("VEVENT").isEmpty()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: ParserException) {
            logger.warn("Skipping calendar: ${e.message}")
        }

        return false
    }

    private fun createCalendar(icsLink: String, rawCalendarData: String, type: CalendarType): OnlineCalendar? {
        logger.debug("Creating calendar entity.")

        val builder = CalendarBuilder()
        val calendar = builder.build(StringReader(rawCalendarData))

        val name = calendar.getProperty("X-WR-CALNAME")?.value
        val color = calendar.getProperty("X-APPLE-CALENDAR-COLOR")?.value
        val description = calendar.getProperty("X-WR-CALDESC")?.value

        return if (name != null && color != null && name != "DEFAULT_TASK_CALENDAR_NAME") {
            OnlineCalendar(
                name,
                color,
                icsLink,
                description,
                CalendarAuth.Nextcloud,
                type,
            )
        } else {
            null
        }
    }


}