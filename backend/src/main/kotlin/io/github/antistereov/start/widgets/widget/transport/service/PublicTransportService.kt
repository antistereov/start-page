package io.github.antistereov.start.widgets.widget.transport.service

import io.github.antistereov.start.user.service.UserService
import io.github.antistereov.start.widgets.widget.location.model.PublicTransportCompany
import io.github.antistereov.start.widgets.widget.transport.model.PublicTransportStop
import io.github.antistereov.start.widgets.widget.transport.model.PublicTransportWidgetData
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class PublicTransportService(
    private val userService: UserService,
) {

    private val logger = LoggerFactory.getLogger(PublicTransportService::class.java)

    fun addStop(userId: String, stop: PublicTransportStop): Mono<Set<PublicTransportStop>> {
        logger.debug("Adding stop: {} to user: {}", stop, userId)

        return userService.findById(userId).flatMap { user ->
            val existingStops = user.widgets.publicTransport.stops
            if (existingStops.add(stop)) {
                userService.save(user).thenReturn(existingStops)
            } else {
                Mono.just(existingStops)
            }
        }
    }

    fun getStops(userId: String): Mono<Set<PublicTransportStop>> {
        logger.debug("Getting stops for user: {}", userId)

        return userService.findById(userId).map { user ->
            user.widgets.publicTransport.stops
        }
    }

    fun deleteStop(userId: String, stop: PublicTransportStop): Mono<Set<PublicTransportStop>> {
        logger.debug("Deleting stop: {} for user: {}", stop, userId)

        return userService.findById(userId).flatMap { user ->
            val existingStops = user.widgets.publicTransport.stops
            if (existingStops.remove(stop)) {
                userService.save(user).thenReturn(existingStops)
            } else {
                Mono.just(existingStops)
            }
        }
    }

    fun addCompany(userId: String, company: PublicTransportCompany): Mono<Set<PublicTransportCompany>> {
        logger.debug("Adding company: {} to user: {}", company, userId)

        return userService.findById(userId).flatMap { user ->
            val existingCompanies = user.widgets.publicTransport.transportCompanies
            if (existingCompanies.add(company)) {
                userService.save(user).thenReturn(existingCompanies)
            } else {
                Mono.just(existingCompanies)
            }
        }
    }

    fun getCompanies(userId: String): Mono<Set<PublicTransportCompany>> {
        logger.debug("Getting companies for user: {}", userId)

        return userService.findById(userId).map { user ->
            user.widgets.publicTransport.transportCompanies
        }
    }

    fun deleteCompany(userId: String, company: PublicTransportCompany): Mono<Set<PublicTransportCompany>> {
        logger.debug("Deleting company: {} for user: {}", company, userId)

        return userService.findById(userId).flatMap { user ->
            val existingCompanies = user.widgets.publicTransport.transportCompanies

            if (existingCompanies.remove(company)) {
                userService.save(user).thenReturn(existingCompanies)
            } else {
                Mono.just(existingCompanies)
            }
        }
    }

    fun getWidgetData(userId: String): Mono<PublicTransportWidgetData> {
        logger.debug("Getting public transport widget data for user: {}", userId)

        return userService.findById(userId).map { user ->
            user.widgets.publicTransport
        }
    }

    fun clearWidgetData(userId: String): Mono<String> {
        logger.debug("Clearing public transport widget for user: {}", userId)

        return userService.findById(userId).flatMap { user ->
            user.widgets.publicTransport = PublicTransportWidgetData()
            userService.save(user).thenReturn("Public transport widget cleared for user: $userId")
        }
    }
}
