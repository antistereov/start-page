package io.github.antistereov.start.util

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class CustomLocalDateTimeDeserializer : JsonDeserializer<LocalDateTime>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): LocalDateTime {
        val rawDate = p.text.removePrefix("/Date(").removeSuffix(")/")
        val timestamp = rawDate.substringBeforeLast(if (rawDate.contains("-")) "-" else "+").toLong()
        val offsetSign = if (rawDate.contains("-")) -1 else 1
        val offsetHours = rawDate.substringAfterLast(if (rawDate.contains("-")) "-" else "+").toInt() * offsetSign
        val zoneId = ZoneId.of("Europe/Paris").normalized()
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), zoneId).plusSeconds(offsetHours.toLong())
    }
}