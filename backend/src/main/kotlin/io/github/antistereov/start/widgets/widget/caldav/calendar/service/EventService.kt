package io.github.antistereov.start.widgets.widget.caldav.calendar.service

import io.github.antistereov.start.security.AESEncryption
import io.github.antistereov.start.widgets.auth.nextcloud.service.NextcloudAuthService
import io.github.antistereov.start.widgets.widget.caldav.base.model.CalDavEntity
import io.github.antistereov.start.widgets.widget.caldav.base.model.RRuleModel
import io.github.antistereov.start.widgets.widget.caldav.base.service.CalDavEntityService
import net.fortuna.ical4j.model.Component
import net.fortuna.ical4j.model.Period
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.model.parameter.Value
import net.fortuna.ical4j.model.property.RRule
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.time.ZoneId

@Service
class EventService(
    nextcloudAuthService: NextcloudAuthService,
    webClientBuilder: WebClient.Builder,
    aesEncryption: AESEncryption,
) : CalDavEntityService(nextcloudAuthService, webClientBuilder, aesEncryption) {

    private val logger = LoggerFactory.getLogger(EventService::class.java)

    override fun filterEntity(entity: Component, period: Period): Boolean {
        if (entity !is VEvent) return false

        val rruleProperty = entity.getProperty<RRule>(RRule.RRULE)
        return if (rruleProperty != null) {
            val seedDate = net.fortuna.ical4j.model.DateTime(entity.startDate.date)
            val periods = rruleProperty.recur.getDates(seedDate, period, Value.DATE_TIME)
            periods.isNotEmpty()
        } else {
            entity.startDate.date.toInstant().isAfter(period.start.toInstant())
        }
    }

    override fun mapEntityToCalDavEntity(entity: Component): CalDavEntity? {
        if (entity !is VEvent) return null

        return CalDavEntity(
            summary = entity.summary.value,
            description = entity.description?.value,
            location = entity.location?.value,
            start = entity.startDate.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),
            end = entity.endDate.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),
            allDay = entity.startDate.isUtc,
            rrule = entity.getProperty<RRule>(RRule.RRULE)?.value?.let { parseRRule(it) },
        )
    }

    override fun parseRRule(rruleString: String): RRuleModel? {
        logger.debug("Parsing RRule.")

        return super.parseRRule(rruleString)
    }
}