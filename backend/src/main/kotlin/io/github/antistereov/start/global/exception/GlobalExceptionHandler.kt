package io.github.antistereov.start.global.exception

import io.github.antistereov.start.global.model.ErrorResponse
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.server.ServerWebExchange

@ControllerAdvice
class GlobalExceptionHandler {

    private val logger: KLogger
        get() = KotlinLogging.logger {}

    @ExceptionHandler(Exception::class)
    suspend fun handleGenericException(ex: Exception, exchange: ServerWebExchange): ResponseEntity<ErrorResponse> {
        logger.error(ex) { "Unexpected exception: ${ex.message}" }

        val errorResponse = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Unexpected Error",
            message = "An unexpected error occurred: ${ex.javaClass.simpleName} - ${ex.message}",
            path = exchange.request.uri.path
        )

        return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
