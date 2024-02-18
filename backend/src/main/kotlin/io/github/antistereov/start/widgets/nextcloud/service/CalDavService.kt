package io.github.antistereov.start.widgets.nextcloud.service


import io.github.antistereov.start.widgets.nextcloud.model.NextcloudCalendar
import io.github.antistereov.start.widgets.nextcloud.model.NextcloudCredentials
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
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
import java.io.StringReader
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory

@Service
class CalDavService {
    fun getFutureEvents(credentials: NextcloudCredentials, calendarName: String): String {
        val calendarUrl = "https://murena.io/remote.php/dav/public-calendars/SQeoXx3G3smbaXDi/?export"

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(calendarUrl)
            .header("Authorization", Credentials.basic(credentials.username, credentials.password))
            .build()

        val response = client.newCall(request).execute()
        val calendarData = response.body?.string()

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
                    // If the event is recurring, check if it occurs within the period
                    val periods = event.calculateRecurrenceSet(period)
                    periods.isNotEmpty()
                } else {
                    // If the event is not recurring, check if it starts after the current time
                    event.startDate.date.toInstant().atZone(now.zone).toLocalDateTime().isAfter(now.toLocalDateTime())
                }
            }

        return eventsToJson(events)
    }

    private fun eventsToJson(events: List<VEvent>): String {
        val jsonEvents = events.map { event ->
            val rruleProperty = event.getProperty(RRule.RRULE) as RRule?

            val start = event.startDate.date
            val end = event.endDate.date
            val allDay = start.toString().length == 8 && end.toString().length == 8

            buildJsonObject {
                put("summary", event.summary.value)
                put("description", event.description?.value ?: "")
                put("location", event.location?.value ?: "")
                put("start", event.startDate.date.toInstant().atZone(ZonedDateTime.now().zone).toString())
                put("end", event.endDate.date.toInstant().atZone(ZonedDateTime.now().zone).toString())
                put("allDay", allDay)
                putJsonObject("rrule") {
                    if (rruleProperty != null) {
                        val rruleParts = rruleProperty.value.split(";").associate {
                            val (key, value) = it.split("=")
                            key to value
                        }

                        rruleParts.forEach { (key, value) ->
                            put(key.lowercase(Locale.getDefault()), value)
                        }
                    }
                }
            }
        }

        val result = buildJsonObject {
            put("events", JsonArray(jsonEvents))
        }

        return Json.encodeToString(result)
    }

    fun getCalendars(credentials: NextcloudCredentials): List<NextcloudCalendar> {
        val calendarUrl = "${credentials.url}/remote.php/dav/calendars/${credentials.username}/"
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(calendarUrl)
            .header("Authorization", Credentials.basic(credentials.username, credentials.password))
            .method("PROPFIND", null)
            .build()

        val response = client.newCall(request).execute()
        val calendarData = response.body?.string()


        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(InputSource(calendarData?.let {
            StringReader(it)
        }))

        val calendarElements = document.getElementsByTagName("d:response")

        val calendars = mutableListOf<NextcloudCalendar>()

        for (i in 0 until calendarElements.length) {
            val calendarElement = calendarElements.item(i) as Element
            val hrefElement = calendarElement.getElementsByTagName("d:href").item(0)
            val icsPath = hrefElement.textContent
            val icsLink = "${credentials.url}$icsPath?export"

            val nameElement = calendarElement.getElementsByTagName("d:displayname").item(0)
            val colorElement = calendarElement.getElementsByTagName("x1:calendar-color").item(0)
            val descriptionElement = calendarElement.getElementsByTagName("d:calendar-description").item(0)

            val name = nameElement?.textContent
            val color = colorElement?.textContent
            val description = descriptionElement?.textContent

            if (name != null && color != null) {
                val calendar = NextcloudCalendar(name, color, icsLink, description)
                calendars.add(calendar)
            }
        }

        return calendars
    }


}