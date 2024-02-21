package io.github.antistereov.start.widgets.widget.caldav.base.service

import io.github.antistereov.start.security.AESEncryption
import io.github.antistereov.start.widgets.auth.nextcloud.service.NextcloudAuthService
import io.github.antistereov.start.widgets.widget.caldav.base.model.CalDavAuthType
import io.github.antistereov.start.widgets.widget.caldav.base.model.CalDavEntity
import io.github.antistereov.start.widgets.widget.caldav.base.model.CalDavResource
import io.github.antistereov.start.widgets.widget.caldav.base.model.RRuleModel
import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.model.Component
import net.fortuna.ical4j.model.Period
import okhttp3.Credentials
import org.slf4j.LoggerFactory
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.io.StringReader
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

open class CalDavEntityService(
    private val nextcloudAuthService: NextcloudAuthService,
    private val webClientBuilder: WebClient.Builder,
    private val aesEncryption: AESEncryption,
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

    private fun buildWebClient(userId: String, resource: CalDavResource): Mono<WebClient> {
        logger.debug("Building web client for resource: ${resource.name} for user: $userId.")

        return when (resource.auth) {
            CalDavAuthType.None -> Mono.just(webClientBuilder.clone().baseUrl(resource.icsLink).build())
            CalDavAuthType.Nextcloud -> {
                nextcloudAuthService.getCredentials(userId).map { credentials ->
                    webClientBuilder.clone()
                        .baseUrl(resource.icsLink)
                        .defaultHeader("Authorization", Credentials.basic(credentials.username, credentials.password))
                        .build()
                }
            }
        }
    }

    private fun updateResource(resource: CalDavResource, resourceEvents: List<CalDavEntity>): CalDavResource {
        logger.debug("Updating resource.")
        return resource.apply {
            this.entities = resourceEvents
        }
    }

    open fun getResourceEntities(resourceData: String): List<CalDavEntity> {
        logger.debug("Getting resource entities.")

        val resourceBuilder = CalendarBuilder()
        val resource = resourceBuilder.build(StringReader(resourceData))

        val now = ZonedDateTime.now()
        val future = now.plusYears(1)

        val nowDate = net.fortuna.ical4j.model.DateTime(Date.from(now.toInstant()))
        val futureDate = net.fortuna.ical4j.model.DateTime(Date.from(future.toInstant()))

        val period = Period(nowDate, futureDate)

        return resource.components
            .filter { filterEntity(it, period) }
            .mapNotNull { mapEntityToCalDavEntity(it) }
    }

    open fun filterEntity(entity: Component, period: Period): Boolean {
        return false
    }

    open fun mapEntityToCalDavEntity(entity: Component): CalDavEntity? {
        return null
    }

    open fun parseRRule(rruleString: String): RRuleModel? {
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

    fun encryptEntities(entities: List<CalDavEntity>): List<CalDavEntity> {
        logger.debug("Encrypting entities.")

        return entities.map { entity ->
            entity.copy(
                summary = aesEncryption.encrypt(entity.summary),
                description = entity.description?.let { aesEncryption.encrypt(it) },
                location = entity.location?.let { aesEncryption.encrypt(it) },
            )
        }
    }

    fun decryptEntities(entities: List<CalDavEntity>): List<CalDavEntity> {
        logger.debug("Decrypting entities.")

        return entities.map { entity ->
            entity.copy(
                summary = aesEncryption.decrypt(entity.summary),
                description = entity.description?.let { aesEncryption.decrypt(it) },
                location = entity.location?.let { aesEncryption.decrypt(it) },
            )
        }
    }
}