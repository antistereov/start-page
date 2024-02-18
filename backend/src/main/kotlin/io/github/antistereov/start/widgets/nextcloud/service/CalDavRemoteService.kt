package io.github.antistereov.start.widgets.nextcloud.service


import io.github.antistereov.start.widgets.nextcloud.model.NextcloudCalendar
import io.github.antistereov.start.widgets.nextcloud.model.NextcloudCredentials
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.stereotype.Service
import org.w3c.dom.Element
import org.xml.sax.InputSource
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

@Service
class CalDavRemoteService {

    fun getRemoteCalendars(credentials: NextcloudCredentials): Flux<NextcloudCalendar> {
        return fetchCalendarData(credentials).flatMapMany { calendarData ->
            parseCalendarData(calendarData, credentials)
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


}