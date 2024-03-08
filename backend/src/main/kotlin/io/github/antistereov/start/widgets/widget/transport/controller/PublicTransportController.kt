package io.github.antistereov.start.widgets.widget.transport.controller

import io.github.antistereov.start.security.AuthenticationPrincipalExtractor
import io.github.antistereov.start.widgets.widget.location.model.PublicTransportCompany
import io.github.antistereov.start.widgets.widget.transport.model.PublicTransportStop
import io.github.antistereov.start.widgets.widget.transport.model.PublicTransportWidgetData
import io.github.antistereov.start.widgets.widget.transport.service.PublicTransportService
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/transport")
class PublicTransportController(
    private val principalExtractor: AuthenticationPrincipalExtractor,
    private val publicTransportService: PublicTransportService,
) {

    private val logger = LoggerFactory.getLogger(PublicTransportController::class.java)

    @PostMapping("/stop")
    fun addStop(
        authentication: Authentication,
        @RequestBody stop: PublicTransportStop,
    ): Mono<Set<PublicTransportStop>> {
        logger.info("Adding stop: {} for user: {}", stop, authentication.name)

        return principalExtractor.getUserId(authentication).flatMap { userId ->
            publicTransportService.addStop(userId, stop)
        }
    }

    @GetMapping("/stop")
    fun getStops(authentication: Authentication): Mono<Set<PublicTransportStop>> {
        logger.info("Getting stops for user: {}", authentication.name)

        return principalExtractor.getUserId(authentication).flatMap { userId ->
            publicTransportService.getStops(userId)
        }
    }

    @DeleteMapping("/stop")
    fun deleteStop(
        authentication: Authentication,
        @RequestParam(required = true) name: String,
        @RequestParam(required = true) city: String,
        @RequestParam(required = true) country: String,
    ): Mono<Set<PublicTransportStop>> {
        logger.info("Deleting stop: {}, {}, {} for user: {}", name, city, country, authentication.name)

        val stop = PublicTransportStop(name, city, country)

        return principalExtractor.getUserId(authentication).flatMap { userId ->
            publicTransportService.deleteStop(userId, stop)
        }
    }

    @PostMapping("/company")
    fun addCompany(
        authentication: Authentication,
        @RequestBody company: PublicTransportCompany,
    ): Mono<Set<PublicTransportCompany>> {
        logger.info("Adding company: {} for user: {}", company, authentication.name)

        return principalExtractor.getUserId(authentication).flatMap { userId ->
            publicTransportService.addCompany(userId, company)
        }
    }

    @GetMapping("/company")
    fun getCompanies(authentication: Authentication): Mono<Set<PublicTransportCompany>> {
        logger.info("Getting companies for user: {}", authentication.name)

        return principalExtractor.getUserId(authentication).flatMap { userId ->
            publicTransportService.getCompanies(userId)
        }
    }

    @DeleteMapping("/company")
    fun deleteCompany(
        authentication: Authentication,
        @RequestParam(required = true) company: PublicTransportCompany,
    ): Mono<Set<PublicTransportCompany>> {
        logger.info("Deleting company: {} for user: {}", company, authentication.name)

        return principalExtractor.getUserId(authentication).flatMap { userId ->
            publicTransportService.deleteCompany(userId, company)
        }
    }

    @GetMapping
    fun getWidgetData(authentication: Authentication): Mono<PublicTransportWidgetData> {
        logger.info("Getting public transport data for user: {}", authentication.name)

        return principalExtractor.getUserId(authentication).flatMap { userId ->
            publicTransportService.getWidgetData(userId)
        }
    }

    @DeleteMapping
    fun clearWidgetData(authentication: Authentication): Mono<String> {
        logger.info("Clearing public transport data for user: {}", authentication.name)

        return principalExtractor.getUserId(authentication).flatMap { userId ->
            publicTransportService.clearWidgetData(userId)
        }
    }

}