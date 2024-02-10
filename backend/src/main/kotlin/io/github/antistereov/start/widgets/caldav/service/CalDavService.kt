package io.github.antistereov.start.widgets.caldav.service


import io.github.antistereov.start.widgets.caldav.model.CalDavCredentials
import io.github.antistereov.start.security.AESEncryption
import io.github.antistereov.start.user.repository.UserRepository
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Service
class CalDavService {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var aesEncryption: AESEncryption

    fun getCredentials(userId: String): CalDavCredentials {
        val user = userRepository.findById(userId).orElseThrow { throw RuntimeException("User not found") }
        return CalDavCredentials(
            aesEncryption.decrypt(user.calDavUrl
                ?: throw RuntimeException("No CalDav URL found for user $userId.")),
            aesEncryption.decrypt(user.calDavUsername
                ?: throw RuntimeException("No CalDav URL found for user $userId.")),
            aesEncryption.decrypt(user.calDavPassword
                ?: throw RuntimeException("No CalDav URL found for user $userId.")),
        )
    }

    fun authentication(userId: String, calDavCredentials: CalDavCredentials) {
        val user = userRepository.findById(userId).orElseThrow { RuntimeException("User not found") }

        val url = if (calDavCredentials.url.endsWith("/")) {
            calDavCredentials.url.substring(0, calDavCredentials.url.length - 1)
        } else {
            calDavCredentials.url
        }

        user.calDavUrl = aesEncryption.encrypt(url)
        user.calDavUsername = aesEncryption.encrypt(calDavCredentials.username)
        // TODO remove encryption for password since it will be encoded in frontend already
        user.calDavPassword = aesEncryption.encrypt(calDavCredentials.password)

        userRepository.save(user)
    }


    private val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")

    fun getEvents(calDavCredentials: CalDavCredentials, calendarName: String): String {
        val calendarPath = "${calDavCredentials.url}/remote.php/dav/calendars/${calDavCredentials.username}/$calendarName"

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

        val credentials = Credentials.basic(calDavCredentials.username, calDavCredentials.password)

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

    fun getCalendars(calDavCredentials: CalDavCredentials): String {
        val calendarsHomeSetPath = "${calDavCredentials.url}/remote.php/dav/calendars/${calDavCredentials.username}"

        val propfindXml = """
            <d:propfind xmlns:d="DAV:" xmlns:c="urn:ietf:params:xml:ns:caldav">
              <d:prop>
                <d:displayname/>
                <c:calendar-color/>
                <c:supported-calendar-component-set/>
              </d:prop>
            </d:propfind>
        """.trimIndent()

        val credentials = Credentials.basic(calDavCredentials.username, calDavCredentials.password)

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