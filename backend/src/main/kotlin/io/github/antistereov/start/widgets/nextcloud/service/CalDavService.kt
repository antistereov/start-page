package io.github.antistereov.start.widgets.nextcloud.service


import io.github.antistereov.start.widgets.nextcloud.model.NextcloudCredentials
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.springframework.stereotype.Service
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Service
class CalDavService {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")

    fun getEvents(nextcloudCredentials: NextcloudCredentials, calendarName: String): String {
        val calendarPath = "${nextcloudCredentials.url}/remote.php/dav/calendars/${nextcloudCredentials.username}/$calendarName"

        val now = ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS)

        val requestXml = """
            <c:calendar-query xmlns:d="DAV:" xmlns:c="urn:ietf:params:xml:ns:caldav">
              <d:prop>
                <d:getetag/>
                <c:calendar-data/>
              </d:prop>
              <c:filter>
                <c:comp-filter name="VCALENDAR">
                  <c:comp-filter name="VEVENT">
                    <c:time-range start="${now.format(dateFormatter)}"/>
                  </c:comp-filter>
                </c:comp-filter>
              </c:filter>
            </c:calendar-query>
        """.trimIndent()

        val credentials = Credentials.basic(nextcloudCredentials.username, nextcloudCredentials.password)

        val mediaType = "application/xml".toMediaType()

        val request = Request.Builder()
            .url(calendarPath)
            .header("Authorization", credentials)
            .header("Depth", "1")
            .method("REPORT", requestXml.toRequestBody(mediaType))
            .build()

        val client = OkHttpClient()

        val response = client.newCall(request).execute()

        return response.body?.string() ?: ""
    }

    fun getCalendars(nextcloudCredentials: NextcloudCredentials): String {
        val calendarsHomeSetPath = "${nextcloudCredentials.url}/remote.php/dav/calendars/${nextcloudCredentials.username}"

        val propfindXml = """
            <d:propfind xmlns:d="DAV:" xmlns:c="urn:ietf:params:xml:ns:caldav">
              <d:prop>
                <d:displayname/>
                <c:calendar-color/>
                <c:supported-calendar-component-set/>
              </d:prop>
            </d:propfind>
        """.trimIndent()

        val credentials = Credentials.basic(nextcloudCredentials.username, nextcloudCredentials.password)

        val mediaType = "application/xml".toMediaType()

        val request = Request.Builder()
            .url(calendarsHomeSetPath)
            .header("Authorization", credentials)
            .header("Depth", "1")
            .method("PROPFIND", propfindXml.toRequestBody(mediaType))
            .build()

        val client = OkHttpClient()

        val response = client.newCall(request).execute()

        return response.body?.string() ?: ""
    }


}