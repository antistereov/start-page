package io.github.antistereov.start.connector.shared.exception

import io.github.antistereov.start.global.model.ErrorResponse
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.server.ServerWebExchange

@ControllerAdvice
class ConnectorExceptionHandler {

    private val logger: KLogger
        get() = KotlinLogging.logger {}

    @ExceptionHandler(ConnectorException::class)
    fun handleConnectorException(ex: ConnectorException, exchange: ServerWebExchange): ResponseEntity<ErrorResponse> {
        logger.error(ex) { "${ex.javaClass.simpleName} - ${ex.message}" }

        val statusCode = HttpStatus.INTERNAL_SERVER_ERROR

        val errorResponse = ErrorResponse(
            status = statusCode.value(),
            error = ex.javaClass.simpleName,
            message = "An error occurred in a connector: ${ex.message}",
            path = exchange.request.uri.path
        )

        return ResponseEntity(errorResponse, statusCode)
    }
}