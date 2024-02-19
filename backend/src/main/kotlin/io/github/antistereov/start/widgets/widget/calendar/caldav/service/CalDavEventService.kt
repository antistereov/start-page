package io.github.antistereov.start.widgets.widget.calendar.caldav.service

import io.github.antistereov.start.security.AESEncryption
import io.github.antistereov.start.widgets.widget.calendar.model.CalendarEvent
import io.github.antistereov.start.widgets.widget.calendar.model.RRuleModel
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class CalDavEventService(
    private val aesEncryption: AESEncryption,
) {

    private val logger = LoggerFactory.getLogger(CalDavEventService::class.java)

    fun encryptEvents(calendarEvents: List<CalendarEvent>): List<CalendarEvent> {
        logger.debug("Encrypting events.")

        return calendarEvents.map { event ->
            CalendarEvent(
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

    fun decryptEvents(calendarEvents: List<CalendarEvent>): List<CalendarEvent> {
        logger.debug("Decrypting events.")

        return calendarEvents.map { event ->
            CalendarEvent(
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