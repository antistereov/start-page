package io.github.antistereov.start.widgets.widget.caldav.service

import io.github.antistereov.start.widgets.auth.nextcloud.service.NextcloudAuthService
import io.github.antistereov.start.widgets.widget.caldav.model.CalDavAuthType
import io.github.antistereov.start.widgets.widget.caldav.model.CalDavEntity
import io.github.antistereov.start.widgets.widget.caldav.model.CalDavResource
import io.github.antistereov.start.widgets.widget.caldav.model.RRuleModel
import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.model.Component
import net.fortuna.ical4j.model.DateTime
import net.fortuna.ical4j.model.Period
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.model.component.VToDo
import net.fortuna.ical4j.model.parameter.Value
import net.fortuna.ical4j.model.property.RRule
import net.fortuna.ical4j.model.property.Status
import okhttp3.Credentials
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.io.StringReader
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class CalDavEntityService(
    private val nextcloudAuthService: NextcloudAuthService,
    private val webClientBuilder: WebClient.Builder,
) {

    private val logger = LoggerFactory.getLogger(CalDavEntityService::class.java)

    fun updateEntities(userId: String, resource: CalDavResource): Mono<CalDavResource> {
        logger.debug("Updating resource entities for resource: ${resource.name} for user: $userId")

        return getEntities(userId, resource)
    }

    private fun getEntities(userId: String, resource: CalDavResource): Mono<CalDavResource> {
        logger.debug("Getting resource entities for resource: ${resource.name} for user: $userId.")

        return buildWebClient(userId, resource).flatMap { client ->
            client
                .get()
                .retrieve()
                .bodyToMono(String::class.java)
                .map { resourceData ->
                    updateResource(resource, getResourceEntities(resourceData))
                }
        }
    }

    private fun buildWebClient(
        userId: String,
        resource: CalDavResource,
        uid: String? = null
    ): Mono<WebClient> {
        logger.debug("Building web client for resource: ${resource.name} for user: $userId.")

        val url = if (uid != null) "${resource.icsLink}/$uid" else resource.icsLink

        return when (resource.auth) {
            CalDavAuthType.None -> Mono.just(webClientBuilder.clone().baseUrl(url).build())
            CalDavAuthType.Nextcloud -> {
                nextcloudAuthService.getCredentials(userId).map { credentials ->
                    webClientBuilder.clone()
                        .baseUrl(url)
                        .defaultHeader(
                            "Authorization",
                            Credentials.basic(credentials.username, credentials.password)
                        )
                        .build()
                }
            }
        }
    }

    private fun updateResource(resource: CalDavResource, entities: List<CalDavEntity>): CalDavResource {
        logger.debug("Updating resource.")
        return resource.apply {
            this.entities = entities.toMutableList()
        }
    }

    private fun getResourceEntities(resourceData: String): List<CalDavEntity> {
        logger.debug("Getting resource entities.")

        val resourceBuilder = CalendarBuilder()
        val resource = resourceBuilder.build(StringReader(resourceData))

        return resource.components
            .filter { filterActiveAndFutureEntities(it) }
            .mapNotNull { mapEntityToCalDavEntity(it) }
    }

    private fun filterActiveAndFutureEntities(entity: Component): Boolean {
        logger.debug("Filtering active and future entities.")

        val threeMonthsAgo = LocalDateTime.now().minusMonths(3)
        val oneYearFromNow = LocalDateTime.now().plusYears(1)

        return when (entity) {
            is VToDo -> entity.status?.value != Status.VTODO_COMPLETED.value
            is VEvent -> {
                val startDate = entity.startDate.date
                if (startDate == null) {
                    false
                } else {
                    val startDateTime = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                    val rruleProperty = entity.getProperty<RRule>(RRule.RRULE)
                    if (rruleProperty != null) {
                        val seedDate = DateTime(Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant()))
                        val recurPeriod = Period(
                            seedDate,
                            DateTime(Date.from(oneYearFromNow.atZone(ZoneId.systemDefault()).toInstant()))
                        )
                        val periods = rruleProperty.recur.getDates(seedDate, recurPeriod, Value.DATE_TIME)
                        periods.any {
                            DateTime(it)
                                .toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime()
                                .isAfter(threeMonthsAgo)
                        }
                    } else {
                        startDateTime.isAfter(threeMonthsAgo) && startDateTime.isBefore(oneYearFromNow)
                    }
                }
            }
            else -> false
        }
    }

    private fun mapEntityToCalDavEntity(entity: Component): CalDavEntity? {
        logger.debug("Mapping entity to CalDavEntity.")
        return when (entity) {
            is VEvent -> {
                CalDavEntity(
                    uid = entity.uid.value,
                    summary = entity.summary?.value ?: "",
                    description = entity.description?.value,
                    location = entity.location?.value,
                    start = entity.startDate.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),
                    end = entity.endDate.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),
                    allDay = entity.startDate.isUtc,
                    rrule = entity.getProperty<RRule>(RRule.RRULE)?.value?.let { parseRRule(it) },
                    status = entity.status?.value,
                    priority = entity.priority?.value?.toInt()
                )
            }
            is VToDo -> {
                CalDavEntity(
                    uid = entity.uid.value,
                    summary = entity.summary?.value ?: "",
                    description = entity.description?.value,
                    location = entity.location?.value,
                    start = entity.startDate?.date?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDateTime(),
                    end = entity.due?.date?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDateTime(),
                    allDay = entity.startDate?.isUtc ?: false,
                    rrule = entity.getProperty<RRule>(RRule.RRULE)?.value?.let { parseRRule(it) },
                    status = entity.status?.value,
                    priority = entity.priority?.value?.toInt()
                )
            }
            else -> null
        }
    }

    private fun parseRRule(rruleString: String): RRuleModel? {
        logger.debug("Parsing RRule.")

        val rruleParts = rruleString.split(";").associate {
            val (key, value) = it.split("=")
            key to value
        }

        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmssX")

        return try {
            RRuleModel(
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
        } catch (e: Exception) {
            logger.error("Error parsing RRULE string: $rruleString", e)
            null
        }
    }
}